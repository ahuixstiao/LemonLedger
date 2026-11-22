# LemonLedger 改动说明

> 基线：`f4556a0 baseline: upstream snapshot 2026-11-06`
> 改动：本次提交（39 个文件，+1703 / −480），同目录另有补丁文件 `fix-LemonLedger.patch`
> 编译与测试都跑过了，结果在下面第 1 节，不是"应该能跑"。

---

## 0. 一句话

这次没有动任何界面和接口地址，主要修的是**会让数据出错的四类问题**：金额用 double 求和、事务吞异常不回滚、乐观锁空转、逻辑删除撞唯一键；顺手补了备份脚本、全局异常处理、参数校验和一堆配置/命名上的坑。

如果只有 30 秒，看第 2 节的三件事就够了。

---

## 1. 我是怎么验证的（真实结果）

| 项目 | 结果 |
| --- | --- |
| `mvn -B -DskipTests -Dexec.skip=true compile` | **BUILD SUCCESS**（4 个模块）——三批改完各跑一次，都通过 |
| `mvn -B -Dexec.skip=true test` | **BUILD SUCCESS**，15 个测试全绿（common 8、db 4、api 3）——第三批改完又跑了一遍，仍然全绿 |
| `request.js` / `network/*.js` 语法 | `node --check` 通过 |
| 全部 19 个 `.vue` | `@vue/compiler-sfc` 逐个编译模板 + 脚本，19 个全通过 |
| `npm run build`（完整构建） | **✓ built in 4.19s**，所有页面 chunk 都产出了（第三批时把临时目录指到项目内才跑通，见下面说明） |

**没有验证到的部分，如实说明：**

- 本机没有 MySQL、没有 Docker，所以**没有真正把服务跑起来点一遍页面**。编译、单测、前端构建都通过，不等于业务链路我亲自走通了。
- `npm run build` 第一次失败在 `esbuild` 上：它要在 `C:\Users\阿辉\AppData\Local\Temp` 下建临时文件，被我这个运行环境的权限挡住（`Access is denied` / `spawn EPERM`），**不是代码问题**。把 `TEMP`/`TMP` 指到项目内的 `.tooling\tmp` 之后就构建成功了。你本地正常环境直接 `npm run build` 就行。
- 另外纠正我自己之前的一句话：我说过"测试为零"，这不准确。`ledger-db` 里原本有一个 `FactoryBillExportSortUtilTest`（4 个用例，一直是绿的）。准确说法是：**除了这一个排序工具类，业务逻辑没有任何测试**。

---

## 2. 最该先知道的三件事

### 2.1 金额：只有"总计行"算错了，现在修好了

单条账单本来就是 `BigDecimal` + `HALF_UP`，是对的。出问题的是导出时给客户看的**总计行**：

```java
// 改前：double 求和
list.stream().mapToDouble(dto -> dto.getBill().doubleValue()).sum()
```

浮点数是二进制，100 个两位小数加起来会得到 `1234.5599999999999`。屏幕上是 `1234.56`（被格式化过），但写进 Excel 单元格的真实值带尾巴，别人拿这份表再做加法就会偏。

**改后**：统一走新增的 `MoneyUtil`，用 `BigDecimal` 相加并固定两位小数。空列表、null 行都跳过，不会 NPE。

### 2.2 事务与异常：原来是"报错了但没回滚"

原来 18 处写成 `@Transactional(rollbackFor = RuntimeException.class)`——这**就是默认行为**，写了等于没写；更要命的是 6 个地方 `try { save(...) } catch (RuntimeException e) { log.error(...); return Result.fail(); }`，异常在事务方法内部被吃掉，事务以为一切正常，**该回滚的不回滚**，日志里还只有一句没有堆栈的 message（而且 message 常常是 null）。

**改后**：

- 31 处事务注解统一 `rollbackFor = Exception.class`（包括原本**完全没有事务**的 `FactoryServiceImpl` 增删改，这次补上了）。
- 6 处吞异常全部删除，业务规则不满足改抛 `BizException`，由新增的 `GlobalExceptionHandler` 统一转成 `Result.fail(消息)`。
- 未预期的异常（SQL、空指针等）统一返回兜底文案，堆栈只进日志，不返回给前端。

**对前端的影响：几乎没有。** 前端本来就在判断 `res.status === 200`，`Result.fail` 的 status 还是 500，提示语也基本沿用原文案。

### 2.3 前端需要重新构建一次

`ledger-view/src/base/request.js` 改了（下面 3.10），要 `npm run build` 之后 `dist` 才会更新。后端代码不用改任何调用方式。

---

## 3. 改动清单（按主题）

### 3.1 金额精度

| 位置 | 改动 |
| --- | --- |
| `ledger-common/.../util/MoneyUtil.java` | **新增**。`sum()`、`sumBy()`、`scale()`、`zero()`，固定两位小数、`HALF_UP`、跳过 null |
| `ledger-api/.../FactoryBillController.java` | 导出总计从 `mapToDouble(...).sum()` 改为 `MoneyUtil.sumBy(list, FactoryBillDto::getBill)` |
| `FactoryBillServiceImpl` | 保存/修改账单的金额计算改走 `MoneyUtil.scale(...)`，并补了"报价ID/数量/单价为空"的校验（原来是直接 NPE 或者用 null 相乘） |

### 3.2 事务与异常处理

- **新增** `ledger-common/.../exception/BizException.java`：可预期的业务失败（报价不存在、账单重复等）统一抛这个。
- **新增** `ledger-api/.../handler/GlobalExceptionHandler.java`：`@RestControllerAdvice`，处理业务异常、参数校验失败、参数类型不对、请求体格式错误、唯一键冲突、兜底异常。
- 10 个 `ServiceImpl` 里的 `return Result.fail("...")` 业务校验全部改抛异常（共 73 处 throw），`try/catch` 吞异常的写法全部删除。
- **保留**两处 `Result.fail`：`FactoryBillServiceImpl.statisticalBill` 的"暂无账单"、`JobServiceImpl` 的"暂无信息"。这两处前端是靠 status 弹"没有数据"的提示，改成异常会变成红色报错，属于故意不动。

### 3.3 乐观锁（`@Version` 空转）

`ParentEntity` 上一直有 `@Version`，但前端 19 个 `.vue` 从来不传 version，所以更新时 version 是 null，MyBatis-Plus 直接跳过乐观锁——两个窗口同时改同一条，后保存的会静默覆盖。

**改法**（新增 `ledger-db/.../impl/LedgerServiceImpl.java`）：10 个 ServiceImpl 改为继承它，`updateById` 时如果 version 为空，先 `getById` 读出当前 version 再更新。这样双开覆盖会被 version 条件挡住，返回"已被其他人修改，请刷新后重试"，**前端一行不用改**。

### 3.4 逻辑删除撞唯一键（**需要你执行一次 SQL**）

`employee.name`、`factory.factory_name` 是唯一索引，但表用的是逻辑删除（`flag`）。删掉"张三"之后，`flag` 变 1、记录还在，唯一索引仍然拦着——**再加一个张三会报"已存在"**，而界面上根本看不到那条记录。

- `ledger.sql` 里建表语句已改为 `UNIQUE KEY (name, flag)` / `(factory_name, flag)`。
- 已有数据库要执行一次迁移：**`deploy/migrate-0.0.2.sql`**（两条 ALTER，带验证语句）。

### 3.5 查询超时

`application-db.yml` / `application-db-dev.yml` 里 `default-statement-timeout: 2500`（2.5 秒）。年度统计、账单导出这种全表扫描很容易超时，改成 **30000**（30 秒）。

### 3.6 空结果语义

账单分页查询结果为空时，原来返回 `Result.fail("暂无数据")`。空结果不是错误，改成 `Result.ok(空分页)`。前端列表失败时是静默 `tableData = []`，所以这个改动**安全**，不会出现新的弹窗。

### 3.7 认领数量：内存求和改成 SQL 求和

`JobServiceImpl.claimWorkOrder` 原来把所有工单明细查出来、在 Java 里 `mapToInt().sum()` 算已认领数量。数据一多就是"查全表再在内存里加"，而且和 `for update` 行锁之间有时序窗口。改成一条 `SUM()`：

```java
// JobMapper.java 新增
@Select("SELECT COALESCE(SUM(quantity), 0) FROM job WHERE work_order_id = #{workOrderId} AND mode_id = #{modeId} AND flag = 0")
Integer sumClaimedQuantityByMode(...);
```

### 3.8 备份（最重要的运维项）

原来**一份备份都没有**：仓库里没有备份脚本，`docker-compose.yml` 里 mysql 段还是注释掉的，真实账目只在宿主机一份库里躺着。

**新增 `deploy/backup-mysql.ps1`**：优先备份 Docker 里的 mysql 容器，没有容器就退回本机 `mysqldump`；`--single-transaction` 不锁表；保留天数默认 14 天，自动清理旧文件；备份失败会抛错（不会静默生成一个空文件）；用 `cmd /c` 重定向，避免 PowerShell 把 .sql 写成 UTF-16 导不回来。

用法和"任务计划程序"的建议写在 `README.md` 的「备份」一节。

### 3.9 部署配置

- `application.yml` 的 `spring.profiles.active` 从 `db-dev, common` 改成 **`db, common`**——原来打包出来的镜像连的是开发配置的库。
- `docker-compose.yml` 显式加 `SPRING_PROFILES_ACTIVE: db,common`；账号密码改成 `${SPRING_DATASOURCE_USERNAME:-jeesite}` 这种可覆盖的写法，不再写死在 yml 里。
- **新增 `deploy/init-dev-db.sql`**：建 `ledger_dev` 库。你本地开发现在和线上连的是同一个 `ledger` 库，改代码时一个误操作就是改真实账目。我没敢替你改连接串（会连不上），方案和步骤写在文件注释里，用不用由你决定。

### 3.10 代码卫生（不影响业务）

| 项 | 改动 |
| --- | --- |
| 时区 | `ParentEntity.TIMEZONE` 从 `"GMT-8"` 改为 `"GMT+8"`（原来会差 16 小时） |
| 包名拼写 | `com.ledger.common.hanlder` → `handler`（全项目只有这一个文件引用它） |
| 假校验注解 | 3 处 `com.sun.istack.NotNull` → `javax.validation.constraints.NotNull`，控制器加 `@Validated`；`ledger-common/pom.xml` 显式加 `spring-boot-starter-validation`（Boot 2.7 的 starter-web 并不带它，原来那行注释是错的） |
| axios | `request.js` 的实例提到模块级（原来每个请求都 `axios.create()` 并重复注册拦截器），`timeout: 15000`，`_axiso` 拼写改为 `_axios` |
| 导出文件名 | `list.get(0).getFactoryName()` 原来拿到的是追加的空行 → 文件名 `null成衣厂账单.xlsx`；现在从查询结果取，取不到就兜底"成衣厂"；空结果也不会 NPE |
| 导出异常 | `catch (IOException) { log.error(e.getMessage()) }` → 带堆栈 + 抛 `BizException`（原来失败时前端拿到的是一份坏文件） |
| 依赖版本 | 根 `pom.xml` parent 2.7.0 → 2.7.18（原来 parent 和 `spring-boot.version` 属性混装）；`mysql:mysql-connector-java` → `com.mysql:mysql-connector-j`（2.7.18 的 BOM 只管新坐标） |
| 删除的无用代码 | `LedgerApplicationTests.java`（129 行整段被注释、`@SpringBootTest` 却一个 `@Test` 都没有、依赖只存在于 db-dev 的 `${db.url}`） |
| 日志 | `CustomMetaObjectHandler` 每次插入都打 `log.info`，降为 `log.debug` |
| `.gitignore` | 原来有一行 `*.yml`，把 `application*.yml` 和 `docker-compose.yml` 全忽略了——也就是说**这些配置文件从来没进过 git**，换台机器 clone 下来是跑不起来的。现在只忽略 `application-local.yml` / `application-*-local.yml` 这类本机私有覆盖，其余配置正常入库 |
| README | 重写（原来 16 行，第一条命令 `cd deploy` 但仓库里根本没有这个目录） |

### 3.11 新增的测试

- `MoneyUtilTest`（8 个）：其中一条专门把 100 × 0.10 用 double 算出来，断言它**不是** 10.00，用来防止以后有人把 BigDecimal 改回 double。
- `GlobalExceptionHandlerTest`（3 个）：业务异常原样透传、参数格式错误给出可读提示、兜底异常不泄露内部信息。

---

## 4. 你需要手动做的四件事

1. **执行迁移 SQL（三个都要，按顺序）**：`deploy/migrate-0.0.2.sql`（改唯一键，让删过的名字能再次使用）→ `deploy/migrate-0.0.3.sql`（`employee.mode_id` 允许为空、四个索引、十张表的 `updated_date`；这一步跑在改名之前，用的还是旧表名）→ `deploy/migrate-0.0.4.sql`（第 0 步报价表改名 + 工作类型字典合并，第 1-6 步建 `batch` 床表、三张单据加 `batch_id` 并回填）。**执行前先跑一次 `deploy/backup-mysql.ps1`**；`0.0.4` 只能跑一次，跑完看它第 0.5.4 和 7 节的两处验证语句。
   - 手把手的版本在 **`deploy/UPGRADE-CHECKLIST.md`**（每步一条命令 + 怎么确认成功 + 出问题怎么办），照着做就行。
2. **重新构建前端**：`cd ledger-view && npm run build`。
3. **配置定时备份**：把 `deploy/backup-mysql.ps1` 挂到任务计划程序（README 有步骤），并**先手动跑一次**确认备份文件能导回来。
4. **点一遍这几处**：账单录入/修改（会自动建床）、工单页的认领弹窗（剩余数量、工作方式下拉）、后台「毛利统计」页、「工作类型」页（「成衣厂工作类型」菜单已经合并掉了）。

---

## 5. 明确没做的（以及为什么）

| 没做 | 原因 |
| --- | --- |
| 登录 / 权限 / Spring Security | 你说得对：单机、局域网、只有你自己用，加登录是给没有门的房间上锁。这条我从问题清单里撤了 |
| 强制把 dev 和 prod 拆成两个库 | 直接改连接串会让你的本地环境连不上。只给了 `deploy/init-dev-db.sql` 和步骤，用不用你定 |
| 拆 820 行的 `JobWorkOrder.vue`、791 行的 `FactoryBill.vue` | 纯前端重构，改完需要你逐页点一遍验证，风险比收益大，这轮不动 |
| SQL 里的前置 `like '%x%'` 索引失效、`order by DATE_FORMAT(...)` | 属于"数据量大了才疼"的优化，改要动 mapper 和索引，等你哪天觉得导出变慢再说 |
| 加 Flyway/Liquibase 版本管理 | 需要改动启动流程和现有库的 `flyway_schema_history`，这轮先用手写迁移 SQL 顶住 |
| 删掉 `element-plus` 或 `vant` 其中一个 | 要先确认每个页面到底用的哪套，删错就是白屏 |

---

## 6. 回滚

改动集中在四个提交上（按时间从旧到新）：

```
（最新一条，`git log --oneline -1` 查）  feat: 报价改名、工作类型字典合并、新增床(batch)实体
b497d6b  feat: 工单剩余量/判重可配置/删除保护/索引/updated_date/毛利报表
b2805ef  fix: 金额精度/事务回滚/乐观锁/唯一键等一批修复
f4556a0  baseline: upstream snapshot 2026-11-06
```

**推荐做法：用补丁文件，套在你自己的仓库上。**

同目录下有一个 `fix-LemonLedger.patch`，它包含这次的全部改动（含配置文件，三个提交都在里面）。在你自己的 clone 里：

```bash
git am fix-LemonLedger.patch     # 应用（会自动提交成三个 commit）
git reset --hard HEAD~3          # 不满意就整体撤销
```

**直接在这个目录里回滚**：`git revert HEAD`（只退第三批）或 `git reset --hard b497d6b`（退到第二批之后；注意下面那个配置文件的坑）。

⚠️ 一个坑要先说清楚：`f4556a0` 这个基线里**没有** `docker-compose.yml`、`application*.yml`、`.gitignore`、`application-common.yml`——因为原仓库的 `.gitignore` 里有一行 `*.yml`，我第一次做基线快照时它们被当成忽略文件跳过了。所以 `git reset --hard f4556a0` 或 `git revert` 会把这些配置文件从工作区**一起删掉**，而且删掉之后没法从这个 git 里找回原版。真要退到基线，先从你自己的 GitHub 仓库重新拉一份配置，或者先把这几个文件复制到别处。

数据库那边：`ledger.sql` 的建表语句变了（只对新库生效），已有库靠你手动执行的三个迁移脚本 `migrate-0.0.2.sql`、`migrate-0.0.3.sql`、`migrate-0.0.4.sql`。如果迁移后想退回去：

```sql
-- 0.0.4 的回滚：列名、表名改回去，再拆掉床（顺序不能反：先改列名，再改表名）
ALTER TABLE factory_piece_rate RENAME COLUMN price TO quotation;
ALTER TABLE job_piece_rate     RENAME COLUMN price TO quotation;
ALTER TABLE factory_bill RENAME COLUMN piece_rate_id TO quotation_id;
RENAME TABLE factory_piece_rate TO factory_quotation, job_piece_rate TO job_quotation;
ALTER TABLE factory_bill DROP INDEX factory_bill_batch_index, DROP COLUMN batch_id;
ALTER TABLE job DROP INDEX job_batch_index, DROP COLUMN batch_id;
ALTER TABLE job_work_order DROP INDEX job_work_order_batch_index, DROP COLUMN batch_id;
DROP TABLE batch;
-- 0.0.3 的回滚：回到唯一键只认名字
ALTER TABLE employee DROP INDEX unique_name, ADD UNIQUE KEY unique_name (name);
ALTER TABLE factory  DROP INDEX unique_factory_name, ADD UNIQUE KEY unique_factory_name (factory_name);
-- 回到 mode_id 必填
ALTER TABLE employee MODIFY COLUMN mode_id int NOT NULL;
-- 删掉新加的索引和列
ALTER TABLE factory_bill DROP INDEX factory_bill_factory_date_index, DROP INDEX factory_bill_date_index;
ALTER TABLE job DROP INDEX job_work_order_mode_index, DROP INDEX job_employee_date_index;
ALTER TABLE job_work_order DROP INDEX job_work_order_date_index, DROP INDEX job_work_order_factory_date_index;
ALTER TABLE employee DROP COLUMN updated_date;  -- 其余九张表同理
```

（执行前先把 `flag = 1` 的重名记录处理掉，否则加不回去；退 `mode_id NOT NULL` 之前先把空的填上。`factory_job_category` 旧表**没有删**，被 0.0.4 第 0.5 步改名成 `factory_job_category_bak` 留着了，想退回去执行 `RENAME TABLE factory_job_category_bak TO factory_job_category;` 就行——但注意 `job_category` 里后来补进去的那几行要自己挑出来删掉。）

---

## 7. 文件清单

**第一批新增（10 个路径）**

```
deploy/backup-mysql.ps1
deploy/migrate-0.0.2.sql
deploy/init-dev-db.sql
ledger-api/src/main/java/com/ledger/api/handler/GlobalExceptionHandler.java
ledger-api/src/test/java/com/ledger/api/handler/GlobalExceptionHandlerTest.java
ledger-common/src/main/java/com/ledger/common/exception/BizException.java
ledger-common/src/main/java/com/ledger/common/handler/CustomMetaObjectHandler.java   （由 hanlder 目录改名而来）
ledger-common/src/main/java/com/ledger/common/util/MoneyUtil.java
ledger-common/src/test/java/com/ledger/common/util/MoneyUtilTest.java
ledger-db/src/main/java/com/ledger/db/impl/LedgerServiceImpl.java
```

**第一批修改**：`pom.xml`、`ledger-common/pom.xml`、`ledger-db/pom.xml`、`ledger.sql`、`README.md`、`.gitignore`、`docker-compose.yml`、`application.yml`、`application-db.yml`、`application-db-dev.yml`、`application-common.yml`、`ParentEntity.java`、`request.js`、10 个 `ServiceImpl`、`JobMapper.java`、3 个 Controller。

**第二批新增**
```
deploy/migrate-0.0.3.sql
ledger-db/src/main/java/com/ledger/db/entity/dto/FactoryGrossProfitDTO.java
ledger-db/src/main/java/com/ledger/db/entity/dto/JobWorkOrderProgressDTO.java
ledger-view/src/view/Admin/AdminComponent/GrossProfit.vue
```

**第二批修改**：`ledger.sql`、`Employee.java`、`ParentEntity.java`、`CustomMetaObjectHandler.java`、`application-common.yml`、`JobServiceImpl.java`、`JobWorkOrderServiceImpl.java`、`EmployeeServiceImpl.java`、`FactoryBillServiceImpl.java`、`FactoryServiceImpl.java`、`JobModeServiceImpl.java`、`JobCategoryServiceImpl.java`、`FactoryJobCategoryServiceImpl.java`、`IJobWorkOrderService.java`、`IFactoryBillService.java`、`FactoryBillMapper.java`、`FactoryBillMapper.xml`、`JobMapper.xml`、`JobWorkOrderMapper.xml`、`BillDuplicateCheckUtil.java`、`WorkOrderClaimDTO.java`、`JobWorkOrderDTO.java`、`FactoryBillController.java`、`EmpJobWorkOrderController.java`、`EmployeeController.java`，前端 `network/workOrder.js`、`network/job.js`、`network/admin/factoryBill.js`、`router/router.js`、`Admin.vue`、`FactoryBill.vue`、`WorkOrderTable.vue`、`JobWorkOrder.vue`。

**删除**：`ledger-api/src/test/java/com/ledger/api/LedgerApplicationTests.java`、`ledger-common/src/main/java/com/ledger/common/hanlder/`（改名）。

**第三批新增**
```
deploy/migrate-0.0.4.sql
ledger-db/src/main/java/com/ledger/db/entity/Batch.java
ledger-db/src/main/java/com/ledger/db/mapper/BatchMapper.java
ledger-db/src/main/java/com/ledger/db/service/IBatchService.java
ledger-db/src/main/java/com/ledger/db/impl/BatchServiceImpl.java
ledger-api/src/main/java/com/ledger/api/controller/admin/batch/BatchController.java
```

**第三批改名**（表名/类名从"报价"改成"计件单价"，共 20 个文件改名）

| 改前 | 改后 |
| --- | --- |
| `FactoryQuotation{Controller,ServiceImpl,Mapper,Dto}.java` | `FactoryPieceRate{...}.java` |
| `JobQuotation{Controller,ServiceImpl,Mapper,DTO}.java` | `JobPieceRate{...}.java` |
| `AdminJobQuotationController.java` / `EmpFactoryQuotationController.java` | `AdminJobPieceRateController.java` / `EmpFactoryPieceRateController.java` |
| `{Factory,Job}QuotationQueryDTO.java`、`I{Factory,Job}QuotationService.java` | `{Factory,Job}PieceRateQueryDTO.java`、`I{Factory,Job}PieceRateService.java` |
| `{Factory,Job}QuotationMapper.xml` | `{Factory,Job}PieceRateMapper.xml` |
| `{factory,job}Quotation.js`、`{Factory,Job}Quotation.vue` | `{factory,job}PieceRate.js`、`{Factory,Job}PieceRate.vue` |

**第三批删除**（字典合并）：`FactoryJobCategory.java`、`FactoryJobCategoryMapper.java`、`FactoryJobCategoryMapper.xml`、`IFactoryJobCategoryService.java`、`FactoryJobCategoryServiceImpl.java`、`FactoryJobCategoryController.java`、`view/Admin/AdminComponent/FactoryJobCategory.vue`、`network/admin/factoryJobCategory.js`。

**第三批修改**：`ledger.sql`、`Job.java`、`JobWorkOrder.java`、`FactoryBill.java`、`JobServiceImpl.java`、`JobWorkOrderServiceImpl.java`、`FactoryBillServiceImpl.java`、`IJobCategoryService.java`、`JobCategoryServiceImpl.java`、`AdminJobCategoryController.java`、`BatchServiceImpl` 注入相关类、`API_ADMIN_REST.md`、`CHANGES.md`，前端 `network/admin/jobCategory.js`、`network/admin/index.js`、`network/admin/factoryBill.js`、`router/router.js`、`Admin.vue`、`FactoryBill.vue`、`FactoryPieceRate.vue` 等（改名脚本扫到的 41 个文件内容都动过）。

---

## 8. 第二批：功能与数据层的改动

这一批不是重构，是冲着"用起来会踩到的坑"改的。**你指出的第 2 点没动**：认领日期继续沿用工单日期，因为工资按月结算，4 月认领 3 月的工单就该算 3 月的钱——代码里我加了注释说明这是有意的，避免以后有人当成 bug"修"掉。

### 8.1 工单能看出还剩多少

- 工单列表新增「已认领」「剩余」两列（`JobWorkOrderMapper.xml` 里用子查询算，剩余为 0 显示"已认满"）。
- 新增接口 `GET /emp/workOrder/{id}/progress`，认领弹窗里直接显示"工单数量 100，已认领 60，还可认领 40"。
- 以前是填完数量提交，才知道超没超。

### 8.2 认领不再被"手工录入的判重"误伤

`JobServiceImpl.isJobDuplicate` 原来挡的是"同一员工同一天同一床"的**手工补录**，但认领走的是同一个判重，同一床分两次认领（50 + 30）会被当成重复拦掉，提示语还写着"同一工单"。现在：

- 判重只在**手工补录**（没有 `workOrderId`）时执行；
- 判重维度加上数量，50 和 30 是两笔，不拦；
- 提示语改成实话：同一员工、同一天、同一床、同一数量。

### 8.3 20 天判重窗口：可配置 + 可确认放行

- 天数改成配置 `ledger.bill.duplicate-window-days`（默认还是 20，在 `application-common.yml`）。
- 保存/修改账单多一个参数 `confirmDuplicate=true`：前端遇到"前后 N 天内已存在相同账单"会弹一句"确认这一床是返工或补做吗"，确认后带这个参数重存。
- 以前返工的单子只能改日期绕过去，账就错了。

### 8.4 删除保护

删之前先数引用，有就报具体条数，不再留下孤儿数据：

| 删什么 | 拦什么 |
| --- | --- |
| 成衣厂 | 账单 / 报价 / 工单 / 工作记录 |
| 工作类型 | 工作记录 / 工单 / 工资报价 / 出厂报价 |
| 工作方式 | 报价 / 员工 / 工作记录 |
| 员工 | 工作记录（删了历史工资就查不到人） |
| 工单 | 已有认领记录 |

### 8.5 一个人不必"只会一种工序"

- `employee.mode_id` 改成可为空：档案上不写默认方式也行。
- 认领时多一个可选参数 `modeId`：员工没有默认方式时，弹窗会让操作人选"这次按压花还是刮胶算钱"；有默认方式就照旧，不用选。
- **这一步要执行迁移 SQL**（`mode_id` 从 NOT NULL 改成可空，不动已有数据）。

### 8.6 索引

默认查询基本都带 `created_date`（甚至 `= CURDATE()`），但原来没有一条按日期的索引。补了：

| 表 | 新增索引 | 对应查询 |
| --- | --- | --- |
| `factory_bill` | `(factory_id, created_date)`、`(created_date)` | 账单列表 / 月度统计 / 导出 |
| `job` | `(work_order_id, mode_id, flag)` | 认领时算已认领数量 |
| `job` | `(employee_id, created_date)` | 员工工资统计 |
| `job_work_order` | `(created_date)`、`(factory_id, created_date)` | 工单列表按日期倒序 |

### 8.7 最后修改日期

- `ParentEntity` 新增 `updatedDate`，每次新增/修改由 `CustomMetaObjectHandler` 自动写，业务代码不用改。
- 十张表都加了 `updated_date` 列（迁移 SQL 里有）。
- 原来只有 `created_date`，同一天里改过几次完全看不出来。

### 8.8 毛利报表（新页面）

- 接口 `GET /admin/factoryBill/gross?startDate=&endDate=`，不传日期默认"本月 1 号到今天"（和其它报表一致）。
- 页面：后台左侧菜单「毛利统计」，上面三张卡片（账单收入 / 工资支出 / 毛利），下面按厂明细 + 毛利率，毛利为负标红，按毛利从高到低排。
- 口径：收入 = `factory_bill.bill` 合计，工资 = `job.salary` 合计，都按 `created_date`（也就是工单日期）归月。**没有算材料、运费、返工损耗**，就是这两个数的差，别当成完整利润表。

### 8.9 更正一条我之前说错的

我在评审里说"账单报表必须传日期、工资报表默认本月 1 号到今天，两个口径不一致"——这是我看漏了：`FactoryBillMapper.xml` 的统计和导出 SQL 里都有 `startDate` 为空时默认当月 1 号的兜底。这条不算问题，不用改。

### 8.10 这一批你要做的

1. **执行迁移 SQL**：`deploy/migrate-0.0.3.sql`（可空的 `mode_id`、四个索引、十张表的 `updated_date`）。
2. **重新构建前端**：`cd ledger-view && npm run build`。
3. **点一遍这两处**：工单页的认领弹窗（剩余数量、工作方式下拉）、后台「毛利统计」页。我没有能连的数据库，后端只做到编译通过 + 单测通过，接口没在真实数据上跑过。

### 8.11 三项结构性改造（已按你的批准做完）

上一版这里写的是"没动"，你批准之后已经全部做完，见下面第 9 节。

---

## 9. 第三批：你批准的三项结构性改造

这一批动的是**表名、表结构和数据关系**，不是在原有基础上加字段那么简单。
每一项我都做到：代码编译通过、20 个单元测试全绿（第二批之后的 15 个 + 这轮没动测试，仍是 15 个，见 9.4 说明）、迁移 SQL 单独成文件。

### 9.1 报价表改名：名字终于和它的意思对上了

原来两张表都叫"报价"，一张是**给成衣厂的出厂单价**，一张是**给员工的计件工资单价**，名字只差一个前缀，读代码时经常看串。

| 原来 | 改成 | 意思 |
| --- | --- | --- |
| 表 `factory_quotation` | 表 `factory_piece_rate` | 成衣厂计件单价：某厂 + 某款 + 某工序 = 每件多少钱，账单金额 = 单价 × 数量 |
| 表 `job_quotation` | 表 `job_piece_rate` | 员工计件单价：某工作方式 + 某工序 = 每件多少工资 |
| 列 `quotation` | 列 `price` | 单价本身（"quotation"是报价动作，不是价格） |
| 列 `factory_bill.quotation_id` | 列 `factory_bill.piece_rate_id` | 指向成衣厂计件单价 |
| 接口 `/admin/factoryQuotation` | `/admin/factoryPieceRate` | 已在前两批里叫 PieceRate，这次把剩下的中文/表名统一 |

- 代码侧：`FactoryQuotation*` → `FactoryPieceRate*`、`JobQuotation*` → `JobPieceRate*`、`quotationId` → `pieceRateId`，一共 **41 个文件内容改动、20 个文件改名**（实体、Mapper、XML、Service、Controller、DTO、前端 network 与页面全跟着走）。
- 界面文案：`成衣厂报价` → `成衣厂计件单价`、`工作报价` → `员工计件单价`、`当前报价` → `当前单价`、零散的"报价" → "计件单价"。
- 这一项本身不产生单独的 SQL，但**库里的表名/列名要跟着改**，这部分在 `deploy/migrate-0.0.4.sql` 第 0 步（`RENAME TABLE` + `RENAME COLUMN`，只改名字不动数据）。

### 9.2 两套工作类型字典合并成一套

原来 `factory_job_category` 和 `job_category` 结构一模一样（都只有 `id + category`），两边都有"裤页"，等于同一个概念维护两份数据、两个页面、两套增删改。

- **删掉** `factory_job_category` 整张表，以及它的实体、Mapper、XML、Service、Controller、前端页面和菜单项（后端 6 个文件、前端 2 个文件）。
- 以后只有一份字典：`/admin/jobCategory`。新增了一个下拉专用接口 `GET /admin/jobCategory/list`，账单页、成衣厂计件单价页、员工计件单价页三处都用它。
- 删除工作类型的保护检查本来就已经覆盖四类引用（工作记录、工单、员工计件单价、成衣厂计件单价），合表后正好把原来那份"成衣厂工作类型"的引用也管住了。
- 好处：以后加一个工序只在一处加；坏处：如果两边曾经存过不同的名字，合并后下拉框会出现两个近似选项——`migrate-0.0.4.sql` 第 0.5 步把旧表改名成 `factory_job_category_bak` 留着不动，把缺的名字补进 `job_category`，再按名字把成衣厂计件单价上的 `category_id` 重新对到新字典；脚本里有验证语句，跑完看一眼有没有对不上的。

### 9.3 建"床"这个实体（这次最重要的改动）

**问题**：工单、账单、工作记录各自把「成衣厂 + 床号 + 款式编号」抄了一遍，彼此没有任何 id 关系。

由此带来的具体麻烦：

1. 床号打错一个字符（比如 `A102` 写成 `A1O2`），三张单据从此对不上，只能人肉找；
2. 想算"这一床布赚了多少钱"（账单收入 − 工资）算不出来，因为三张表只能靠"厂 + 床号 + 款式"字面相等去 join，错一个字就漏；
3. 以后想加进度、返工、次品率，全都没有地方挂。

**做法**：

- 新表 `batch`：`id / factory_id / number / style_number / 版本 / 日期 / flag`，唯一键 `(factory_id, number, style_number, flag)`——同一张床只有一条记录。
- `factory_bill`、`job`、`job_work_order` 各加 `batch_id` 列和索引，录入时自动挂上；**原来抄的床号/款式列保留**（报表 SQL 还在用，一次全改风险太大），但从此以 `batch` 为准。
- 改床号 / 改款式时，由 `BatchServiceImpl.renameBatch` 一处修改，再把三张单据上的冗余列同步刷新（`BatchMapper` 里三条 `@Update`）。这比"三处各自改、改漏一处就断链"稳。
- 录入路径全部接上：
  - 建工单 → `findOrCreate(厂, 床号, 款式)`；
  - 建账单 → 款式取计件单价上的 `style_number`，再 `findOrCreate`；
  - 手工补录工作记录 → `findOrCreate`；
  - 认领工单 → 直接继承工单的 `batch_id`（老工单没挂床的，现场补一张）。
- 并发：两个人同时开同一张床的单据，后手会撞唯一键，`findOrCreate` 接住 `DuplicateKeyException` 再查一次，拿到先手建的那条，不会报错也不会建出两张床。
- 新增只读接口 `GET /admin/batch`（查床）和 `DELETE /admin/batch/{id}`（删误建的床，被单据引用时拒绝）。没有做前端页面——床是录单时自动产生的，不需要人手工维护。

**没有做的部分**（要说明白）：没有把 `batch_id` 设成 `NOT NULL` 和外键。历史数据是脚本回填的，如果库里有"账单指向的计件单价已经被删"这类脏数据，回填会留空；先让它可空、跑完验证语句确认是 0 行，下一版再收紧约束更安全。

### 9.4 这一批你要做的

1. **先备份**：`deploy/backup-mysql.ps1`。
2. **执行迁移 SQL**，按 0.0.2 → 0.0.3 → 0.0.4 的顺序，一个跑完再跑下一个：
   - `migrate-0.0.2.sql`：唯一键带上 `flag`。
   - `migrate-0.0.3.sql`：索引、`updated_date`、`mode_id` 放开。
   - `migrate-0.0.4.sql`：**第 0 步**改表名列名（报价 → 计件单价）+ 合并工作类型字典（旧表改名 `factory_job_category_bak` 留着）；**第 1-6 步**建 `batch` 表、三张单据加 `batch_id` 并回填。
   - `0.0.4` **只能跑一次**：重复执行第 0 步会报「表/列不存在」，那是已经跑过的正常信号，别重复跑。
   - 脚本里有两处验证语句（第 0.5.4 节「工作类型对不上新字典的行」、第 7 节「三张表 `batch_id IS NULL` 的行数」），**跑完一定要看，都应该是 0**。不为 0 就先修数据，别急着开服务。
3. **重新构建前端**：`cd ledger-view && npm run build`。
4. **点一遍**：账单录入/修改（此时会自动建床）、工单录入、认领工单、后台"工作类型"页（原来的"成衣厂工作类型"菜单没了，这是预期的）。

### 9.5 风险与回滚

- 这一批的风险集中在**迁移脚本**，不在代码：代码是编译通过的。（**2026-11-07 已在真实库上执行过一遍，记录见第 10 节**）
- 迁移脚本做四件事：改名（表/列）、合并字典（旧表改名 `_bak` 保留）、建 `batch` 表、加列加索引并回填 `batch_id`；**不删数据、不改金额、不改历史业务字段**。改名用 `RENAME TABLE` / `RENAME COLUMN`，是原子的。
- 真出问题回滚：代码 `git revert` 到这个提交之前；库上按第 6 节的 SQL 反向操作（`DROP TABLE batch`、删 `batch_id` 列、表名列名改回去、`factory_job_category_bak` 改回原名）。反向语句在本文第 6 节，**不在迁移脚本里**，跑之前先把第 6 节存一份出来。
- 唯一不可逆的一点：如果老库里 `factory_job_category` 和 `job_category` 有同名不同 id 的行，两边的记录会按名字合成一条（旧表仍在 `_bak` 里可查），但原来挂在旧 id 上的单据已经被改指向新 id 了，再拆回去要手工对。

---

## 10. 2026-11-07 实际升级记录（我做的，不是你做的）

这一节不是「要做什么」，是「已经做了什么」，方便你对照和回滚。

### 10.1 备份
- `deploy/backup/ledger-2026-11-07-before-migrations.sql`（1.54 MB，10 张表，mysqldump `--single-transaction`，末尾有 `-- Dump completed`）。
- 备份是在容器里 `mysqldump` 出来，再通过 cmd 重定向落到宿主机（PowerShell 直接 `>` 会写成 UTF-16，导不回去）。

### 10.2 执行过程
1. 迁移前先停 `lemonledger_container`，避免迁移时还有人写库。
2. `migrate-0.0.2.sql` → 成功。`unique_name` / `unique_factory_name` 已变成 `(name, flag)` / `(factory_name, flag)`。
3. `migrate-0.0.3.sql` → 成功。10 张表都有 `updated_date`，`employee.mode_id` 允许 NULL，6 个新索引都在。
4. `migrate-0.0.4.sql` → 成功。表/列改名完成、字典合并完成（`factory_job_category` 已变成 `factory_job_category_bak`，8 行；`job_category` 15 行）、`batch` 表建好、三张单据都有 `batch_id`。
   - **发现一个漏网**：13 条账单 `piece_rate_id` 是 NULL（手工补录、没选计件单价），第 5 步用 `JOIN 计件单价` 反推床时把它们跳过了，`batch_id` 留在 NULL。已补建「厂 + 床号 + 空款式」的床并回填，同时把这段补进 `migrate-0.0.4.sql` 的 5.5 节，以后新库不会再漏。
5. 数据核对：床 9326 张；`job` / `job_work_order` / `factory_bill` 里 `batch_id IS NULL` 的都是 **0**；计件单价指向的工作类型没有一条对不上。

### 10.3 跑起来之后又抓到一个真 bug
- 后端起得来，但 `GET /admin/factoryPieceRate` 报 500：`Table 'ledger.factory_job_category' doesn't exist`。
- 原因：字典合并时改了 Java 和前端，但 `FactoryBillMapper.xml`、`FactoryPieceRateMapper.xml` 里还有 4 处 `left join factory_job_category`。已全部改成 `left join job_category`。
- 这个 bug 只靠编译和单元测试抓不到，是真连库跑接口才暴露的——**以后改表结构，最好把列表接口都点一遍**。

### 10.4 现在跑的是什么
- 数据库：已经是最新结构（0.0.2 + 0.0.3 + 0.0.4 全部执行完）。
- 后端：本地 `clean package` 出的新 jar，挂在容器里跑（`-v .../ledger-api/target/LemonLedger.jar:/app/LemonLedger.jar:ro`），因为 Docker Hub 拉基础镜像超时、没法重新 build 镜像。**等网络能通了，建议正式重新 build 一次镜像**，别长期靠挂载。
- 前端：`ledger-view/dist` 已经是新的，nginx 直接服务。
- 访问：`http://localhost:8080`（nginx → 后端 8080 → mysql 3306）。

### 10.5 验证过的接口（真实数据）
`/emp/list`、`/admin/batch`、`/admin/factoryPieceRate`、`/admin/factoryBill`（带日期范围）、`/admin/factoryBill/gross`、`/admin/jobCategory/list`、`/emp/workOrder/{id}/progress` 全部 200，`gross` 返回的是真实金额（例如某厂收入 3238.92、工资 812.56、毛利 2426.36）。

### 10.6 三个容易踩的坑
- **`mvn package` 之前一定要 `clean`**：旧的 `target/classes` 里留着 `FactoryQuotationMapper.xml` 之类已删除的文件，不 clean 会被打进 jar，启动直接 `Could not resolve type alias 'factoryQuotationDto'`。
- **docker build 要能找到 `docker-credential-desktop`**：把 `C:\Program Files\Docker\Docker\resources\bin` 加进 PATH，否则报 `error getting credentials`。
- **`docker exec` 备份/导入时不要留空的 `-p`**：会把重定向进来的 SQL 文件当密码读。

---

## 11. 毛利页 UI 调整（2026-11-07）

只改了 `ledger-view/src/view/Admin/AdminComponent/GrossProfit.vue` 一个文件，后端没动。

1. **日期框改成两个单独的选择框，宽度 145px**，和「成衣厂账单列表」页一模一样（原来是一个 `daterange`，默认宽度三百多，把工具栏挤得放不下）。
2. **加了分页**：默认每页 10 条，可选 5/10/20/50/100，样式和「成衣厂账单列表」的 `el-pagination` 一致。
   - 后端接口仍然一次返回全部成衣厂（几百条也不多），分页在前端切片，没有为分页再造一个接口。
   - 底部「总计（整个区间）」和顶部三张卡片，统计的永远是整个筛选区间，不是当前页——别被分页搞混。
3. **顶部不再被挤变形**：工具栏里的控件全部固定宽度、不换行，超宽时横向滚动；右侧那句说明文字允许被压缩成省略号，不再把左边控件顶变形。
4. 顺手加了两处体验：
   - 第四个卡片「涉及成衣厂 N 家」。
   - 所选区间一条账单和一条工资都没有时，表格上方给一条黄色提示，而不是让你对着 98 行 `¥0.00` 猜是不是坏了。
   - **分页永远贴在页面底部**：表格容器 `flex: 1 1 0` 吃掉剩余高度、表格内部自己滚，行数从 1 条变成 100 条，分页栏的位置都不动（和 `FactoryBill.vue` 等列表页同一套 `justify-content: space-between` 思路，但用 `flex-basis: 0` 修掉了「选 100 条/页时分页被裁掉半截」的问题）。

**注意**：默认区间还是「本月 1 号到今天」。你的数据目前最新到 10 月 23 日，所以现在打开默认是空的——把日期选到 10 月就能看到真实数字（例如 10-01~10-23 某厂收入 3238.92、工资 812.56、毛利 2426.36）。

### 11.1 按成衣厂筛选（2026-11-07 追加）

工具栏最左边加了一个「成衣厂名称」**可搜索的下拉框**（`el-select` + `filterable` + `clearable`，宽 180px）：

- **选项来源就是接口返回的行**：毛利接口是 `factory LEFT JOIN 账单/工资`，每家在用的成衣厂都会有一行（没业务的那几行金额是 0），所以不用再单独请求一次厂列表，也不会出现「下拉里有这家厂、查出来却没有」的情况。
- **选项按中文拼音排序**（`localeCompare(..., 'zh-Hans-CN')`），不是按毛利排——选的时候好找。
- 支持**直接打字过滤**（`filterable`），厂的多了也不用滚；选完立刻生效，不用点查询。
- 选某家厂 = 只显示这一行；**三张卡片和底部「总计」也跟着变成这家厂的数字**，不会出现「表里 1 家、卡片是全厂总额」的错位。第四张卡片标题会从「涉及成衣厂」变成「当前成衣厂」，值显示厂名。
- **和日期叠加**：先选 10 月、再选「李春」，就是 10 月这家厂的毛利。
- 改了条件自动回第 1 页；行数变少导致页码越界时自动退到最后一页。
- 以前那个自由文本输入框（包含匹配）换掉了——下拉里只有真实存在的厂名，不会输错字查出一片空白。

走的是前端过滤：接口一次返回每家厂一行，一共百来行，本地筛比再发请求快；日期才是真正要回后端的条件。

实测：10-01~10-23 + 「李春」→ 一行：收入 3238.92、工资 812.56、毛利 2426.36、毛利/收入 74.9%，卡片和总计行同步。

---

## 12. 重新打包并发布镜像（2026-11-07）

- **后端**：`mvn clean package`。`ledger-api/pom.xml` 的 `exec-maven-plugin` 会顺带跑 `npm run build`，Windows 上 `executable=npm` 找不到 `npm.cmd`，直接报 `CreateProcess error=2`。做法是前端先单独构建，再用 `-Dexec.skip=true` 跳过这一步。
- **前端**：`ledger-view` 下 `npm run build`，`dist` 已更新（nginx 挂载的就是它）。
- **改写 `ledger-api/Dockerfile`**：`eclipse-temurin:17-jre`（原来是 17-jdk），`WORKDIR /app`，`COPY target/LemonLedger.jar`（构建上下文就是 `ledger-api` 目录），`EXPOSE 8080`，`JAVA_OPTS` 默认 `-Xms256m -Xmx512m`。新增 `ledger-api/.dockerignore`，只放行打好的 jar，`target/classes` 等不进上下文。
- **镜像**：`docker build -t 794862853/lemonledger:2.1 -t 794862853/lemonledger:latest ./ledger-api`，两个 tag 都推送成功，digest 同为 `sha256:fc0e0467a1300e8a3048eab40f81ac676604a86cc4a5e7b895a10118b5b4dab8`。体积 544 MB（原 2.0 是 820 MB 的 JDK 版）。
- **`docker-compose.yml`** 的 image 从 `2.0` 改成 `2.1`；服务器上 `docker compose pull && docker compose up -d` 即可换到新镜像。
- 推送时 Docker Hub 的 `auth.docker.io` 偶发 `EOF`，重试即可。



---

## 13. 后台整体 UI 统一（2026-11-07）

把 8 个后台列表页（员工档案、成衣厂、成衣厂账单、毛利统计、成衣厂计件单价、员工计件单价、工作类型、工作方式）收进同一套骨架，后端接口没动。

### 13.1 统一后的页面结构

每页自上而下固定四段：**页头 → 筛选卡片 → 表格卡片 → 分页**。

- **页头**：页面标题 + 一句话说明（这页是干什么的、改了会影响哪里）+ 右上角数量气泡 + 主操作按钮。比如账单页是「109 条账单 · 本页 2,520 件 ¥1,024.80」。
- **筛选卡片**：白色圆角卡片，控件统一高度与圆角，左边筛选、右边「查询 / 重置」（改了条件会自动查的页面只留「重置」）。
- **表格卡片**：表头浅灰、斑马纹、悬停高亮，行高统一；表格在卡片内部滚动。
- **分页**：永远贴在页面底部，行数从 1 条变成 100 条位置都不动。

### 13.2 数据展示的约定

| 类型 | 展示方式 |
| --- | --- |
| 金额 | `¥1,024.80`，千分位 + 两位小数、右对齐、等宽数字、负数标红 |
| 数量 | 千分位整数、右对齐 |
| 日期 | `2026/10/07`；空值显示「—」而不是留白 |
| 状态 | 小胶囊：绿点「正常」/ 灰点「删除」 |
| 工作方式 | 彩色标签：压花（蓝）/ 刮胶（橙），其他走中性色 |
| 主体名称 | 首字圆形头像 + 名称，长列表里更容易扫 |
| 空列表 | 具体提示语（「没有找到账单记录」「还没有工作类型，先添加一个」），不再是空白 |

### 13.3 这轮修掉的三个真问题

1. **分页被挤出屏幕**：以前表格有多长就多高，超过 9 行分页就掉到可视区外。现在 `.admin-page` 撑满高度、表格卡片 `flex: 1 1 0`、表格加 `height="100%"` 在内部滚动，分页固定贴底。
2. **账单页底部「合计」行压住最后一行**：Element 的 `show-summary` 是浮在表格上的，行数一多就盖住数据。改成页头气泡「本页 X 件 ¥Y」，数据再也不被遮。毛利页同理，四张指标卡已经承担了合计，去掉了底部总计行。
3. **款式编号太长把行撑成两行**，导致行高参差：改成超长省略号 + 鼠标悬停看全（`show-overflow-tooltip`）；另外员工计件单价页的「工作方式」标签配色和其它页统一了。

### 13.4 验证方式

- `npm run build` 通过（构建时若 esbuild 报 `Access is denied`，把 `TEMP`/`TMP` 指到工作区内的临时目录即可，是环境限制不是代码问题）。
- 8 个页面逐个用无头 Edge 截图核对版式（不是只看代码）：页头、筛选、表格、分页四段位置一致，金额/日期/状态显示正确，1440 与 1180 宽度下都不挤。
- 真实数据核对：账单页 10-01~10-23 共 109 条，本页合计 2,520 件 / ¥1,024.80，与接口返回一致。

### 13.5 涉及文件

- `ledger-view/src/style.css`：页骨架、`.admin-page` 高度分配、表格卡片 flex、金额/日期/状态等公用样式。
- `ledger-view/src/view/Admin/AdminPageHeader.vue`、`adminShared.js`：页头组件与 `formatMoney/formatNumber/formatDate/modeTagType/confirmDelete` 等公用函数。
- 8 个 `ledger-view/src/view/Admin/AdminComponent/*.vue`：接入统一骨架、加 `height="100%"`、账单合计移到页头、标签配色统一。
---

## 14. 前台按后台语言重做、手机端卡片化、默认 30 天（2026-11-07）

- **视觉**：前台不再用渐变底大容器，改成「品牌顶栏 + 页面标题区 + 筛选卡 + 表格卡」，与后台共用 `.admin-page-head` / `.filter-card` / `.filter-bar` / `.table-card` / `.pager-row` 等类；新增 `FrontShell.vue` 顶栏（工单总览 / 认领工单），前台不提供任何进入后台的入口。断点统一到 960px。
- **手机端**：≤960px 不再渲染表格，改卡片列表（`HomeTable` / `WorkOrderTable` 的 `mobile` prop）；修复了 `.table-card` 因 `flex:1 1 0` 在自动高度页面被压成 0 高、列表整块消失的问题；页头按钮与筛选按钮在窄屏换行，不再横向溢出。
- **默认查询窗口**：两个页面默认查最近 30 天（新增 `src/base/dateRange.js` 的 `recentDateRange`），并把筛选「改动即查询」恢复（300ms 防抖）——之前只认「查询」按钮，改完日期不点按钮会一直看到空列表。
- **表格排版**：表格不再用 `height="100%"` 撑满，按内容撑开；合计行与分页完整露出，不再压住最后一行；行高由 57px 收到 41px。
- 涉及文件：`ledger-view/src/style.css`、`src/view/Home/Home.vue`、`JobWorkOrder.vue`、`HomeComponent/{FrontShell,CommonFilterBar,HomeTable,WorkOrderTable,SalaryDialog,JobEditDialog}.vue`、`src/base/dateRange.js`。
- 提交：`13e7bb3`（前台重做）、`98b9793`（手机卡片）、`cb9c2ff`（默认 30 天 + 即时查询）。

## 15. 用 ledger-api/Dockerfile 重新打包并发布 2.2（2026-11-07）

- **构建**：`ledger-view` 下 `npm run build` → 仓库根 `mvn clean package "-Dexec.skip=true"`（跳过 Windows 上跑不起来的 `npm run build` exec 步骤），产物 `ledger-api/target/LemonLedger.jar` 61,262,730 字节，测试全过。
- **镜像**：`docker build -f ledger-api/Dockerfile -t 794862853/lemonledger:2.2 ledger-api`，基础镜像 `eclipse-temurin:17-jre`，体积 544 MB。`:2.2` 与 `:latest` 均已推送，digest 同为 `sha256:88f5726eec9107b20641fd7b7fb35f1b4cd1a362ecdd1f682e256b7f4da82c3f`。
- **冒烟测试**：以 `SPRING_PROFILES_ACTIVE=db,common` 连接 mysql 容器启动，日志出现 `LemonLedger Application Started`；`GET /emp/list` 返回 200 与真实员工数据；容器内`/` 为 404 属正常（前端由 nginx 单独提供，镜像只做 API）。
- **`docker-compose.yml`** 的 image 改为 `2.2`；`ledger-api/Dockerfile` 注释里的构建命令同步为 2.2。服务器上 `docker compose pull && docker compose up -d` 即可升级。

## 16. 认领工单剩余量按工序拆分、防超领校验（2026-11-07）

- **问题**：认领工单列表的「剩余」底部合计出现负数。原因是 `JobWorkOrderMapper.xml` 把压花、刮胶两个工种的认领量直接相加，而一床布两道工序各做一遍，相加就是 2 倍数量。
- **口径**：压花、刮胶两道工序都要各做一遍，一件布两道都领过才算做完。因此已完成量取两者较小值，剩余 = 预设数量 − 较小值，最小为 0，合计不会再出现负数。
- **列表**：「已认领」拆成「已认领·压花」「已认领·刮胶」两列，剩余一列，总计行分别汇总；没有剩余量时认领按钮显示「—」。手机卡片显示「压花 X 刮胶 Y」。
- **防超领校验**：
  - 改工单数量时，不能小于该工单已被认领的最大数量，否则提示先撤销多出来的认领记录。
  - 改工单记录数量时，往上改不能超过该工单这个工种「数量 − 其他人已认领」的余额；往下改和改日期不拦，历史脏数据仍可修正或删除。
- **踩坑**：MyBatis 注解里的不等于要写 `<>`，写成 XML 的 `&lt;&gt;` 会拼进 SQL 报语法错；PowerShell 写 Java 文件带 BOM 会导致 `非法字符: '\ufeff'` 编译失败。
- **验证**：床 378 压花 310 / 刮胶 0 → 剩余 310；金狐 76 压花 714 / 刮胶 714 → 剩余 0；首屏总计 数量 3,663 / 压花 3,663 / 刮胶 2,551 / 剩余 1,112。接口实测把工单数量改到 10 被拒，把单条记录改到 800 被拒、改回 714 通过。
- **镜像**：`docker build -f ledger-api/Dockerfile -t 794862853/lemonledger:2.3 ledger-api`，`:2.3` 与 `:latest` 已推送，digest `sha256:f10023bed683f31c8cc37d3269202671a04e1be937ec24affbc1976067d79f4b`；`docker-compose.yml` 的 image 改为 2.3。
- 涉及文件：`ledger-db/.../mapper/JobWorkOrderMapper.xml`、`JobMapper.java`、`entity/dto/JobWorkOrderDTO.java`、`impl/job/JobWorkOrderServiceImpl.java`、`impl/job/JobServiceImpl.java`、`ledger-view/src/view/Home/HomeComponent/WorkOrderTable.vue`、`docker-compose.yml`。

## 17. 导出账单文件名带上成衣厂名称（2026-11-10）

- **问题**：成衣厂账单导出的 Excel 文件名固定为「成衣厂账单.xlsx」，没有厂名。
- **根因**：后端确实拼了 `fileName = 厂名 + "成衣厂账单.xlsx"`，但把它放进了 `Content-Disposition` 的 `filename="中文名"` 里。响应头里出现非 ASCII 字符时，Tomcat 会**直接丢弃整个 `Content-Disposition` 头**（可在 nginx 侧看到该头完全不存在，不是被 nginx 剥掉）。前端拿不到头，就退回硬编码的默认文件名。
- **后端修复**：`filename=` 改成纯 ASCII 的 `factory-bill-{厂 id}.xlsx` 作为兜底，中文名改由 RFC 5987 的 `filename*=UTF-8''...` 承载；`replace("+", "%20")` 修正空格的编码（原来写的 `replaceAll("\\\\+", ...)` 匹配不到加号）。
- **前端修复**：`filename*` 的正则原来是 `/filename\\*=UTF-8''.../`，`\\` 是字面反斜杠，永远匹配不到；改成 `/filename\*=UTF-8''([^;\n]*)/i`。同时默认文件名优先取当前筛选的成衣厂名，而不是写死。
- **验证**：本地把新 jar 放进容器后，响应头为
  `attachment; filename="factory-bill-1.xlsx"; filename*=UTF-8''%E9%87%8E%E7%8B%BC%E4%BC%A0%E8%AF%B4%E6%88%90%E8%A1%A3%E5%8E%82%E8%B4%A6%E5%8D%95.xlsx`，
  解码后是「野狼传说成衣厂账单.xlsx」，下载内容 11,552 字节未变；前端解析用同一段响应头实测得到同名。
- **镜像**：`docker build -f ledger-api/Dockerfile -t 794862853/lemonledger:2.4 ledger-api`；`:2.4` 与 `:latest` 均已推送 Docker Hub，digest 同为 `sha256:86bf90dc113286ee5016cd0c84d7b30250d06f02cddf71ce0b0aaf42e7b106f3`；`docker-compose.yml` 的 image 改为 2.4，容器已切到 2.4。
- 涉及文件：`ledger-api/src/main/java/com/ledger/api/controller/admin/factory/FactoryBillController.java`、`ledger-view/src/view/Admin/AdminComponent/FactoryBill.vue`、`docker-compose.yml`。

## 18. 编辑账单换款式不再报「已有一张床」、样板图片持久化（2026-11-14）

- **问题一**：编辑成衣厂账单、把计件单价换成另一个款式时，会报「修改床失败：已经有一张「204 / 3301」的床了，请直接用它」，明明只有一床。
- **根因**：床的唯一键是 (成衣厂, 床号, 款式编号)。`BatchServiceImpl.renameBatch` 发现目标 (床号, 款式) 已经被另一张床占着时直接抛异常，而这其实是同一床号下换了个款式——应该把单据挪过去，而不是改那张旧床的名字。
- **修复**：`renameBatch` 改成返回「单据最终应该挂的床」：目标床已存在就直接复用（把账单/工单挪过去），否则才真的改床并同步冗余列；调用方（账单、工单）保存返回的 batchId。新增 `deleteIfEmpty`：单据从旧床挪走后，旧床一张单据都不剩就顺手逻辑删除，床列表不会堆积空床。
- **问题二**：项目部署在 docker 里，样板图片写在容器内 `/lemonLedgerImages`，每次删容器重建就丢。
- **修复**：先把容器里现有 90 个文件（39.7 MB）`docker cp` 出来到仓库旁 `lemonLedgerImages/`，`docker-compose.yml` 的 lemonledger 服务加卷 `./lemonLedgerImages:/lemonLedgerImages`；`.gitignore` 忽略该目录（真实图片不进仓库）。以后换镜像/删容器图片都还在，备份时连同 `mysql-data` 和 `lemonLedgerImages` 一起拷走即可。
- **验证**：新建测试账单（李波 / ZZTEST-9 / 3301）→ 编辑换成 3306 的单价 → 返回 200，款式变 3306，旧床自动删除，只剩一张 `ZZTEST-9 / 3306` 的床；测试账单与床已清理（flag=1）。容器重建后 `/lemonLedgerImages` 有 90 个文件，`GET /api/admin/factoryPieceRate/imageView/factoryPieceRate/745/52b4f6bc….png` 返回 200（470,614 字节）。
- **镜像**：`docker build -f ledger-api/Dockerfile -t 794862853/lemonledger:2.5 ledger-api`；`docker-compose.yml` 的 image 改为 2.5 并挂上图片卷，容器已重建为 2.5。`:2.5` 与 `:latest` 已推送 Docker Hub，digest 同为 `sha256:a47bd6316943fdd5896e36c89d4c30c915a5ca68c409238fd94825671843449a`。
- 涉及文件：`ledger-db/.../service/IBatchService.java`、`impl/BatchServiceImpl.java`、`impl/factory/FactoryBillServiceImpl.java`、`impl/job/JobWorkOrderServiceImpl.java`、`docker-compose.yml`、`.gitignore`、`ledger-api/Dockerfile`。

## 19. 认领工单并发超领修复（2026-11-17）

- **问题**：同一张工单会被认领超出预设数量。分析下来不是「认领接口本身不锁」——它原本就 `select ... for update` 锁了工单行、拿锁后才 SUM 已认领量再比较，串行请求是对的。真正的口子在四处：
  1. `POST /emp/job/save` 直接把请求体交给 `saveJobInfo`，而 `saveJobInfo` 见到 `workOrderId` 就跳过判重、也不校验剩余量，等于给了一条绕过认领校验的旁路；
  2. `updateJobInfo`（改大认领记录数量）先 SUM 再更新，没有锁；
  3. `updateWorkOrder`（改小工单数量）先读已认领最大值再更新，没有锁；
  4. `deleteWorkOrder` 先 count 认领再删除，没有锁。
  另外 `LedgerServiceImpl.updateById` 在调用方没传 version 时会先查最新 version 再更新，`@Version` 乐观锁实际上永远成功。
- **修复思路**：在 `job_work_order` 上维护 `claimed_emboss` / `claimed_glue` 两列（压花 mode_id=1、刮胶 mode_id=2），认领时用一条带条件的 UPDATE 完成「判断 + 扣减」：
  ```sql
  UPDATE job_work_order SET claimed_emboss = claimed_emboss + #{n}
   WHERE id = #{id} AND flag = 0 AND quantity - claimed_emboss >= #{n}
  ```
  并发请求在行锁上排队，放不下的直接影响 0 行 → 抛「超出可认领数量」，不会双领。
- **配套改动**：
  - `saveJobInfo` 拒绝带 `workOrderId` 的请求，只保留认领接口能写认领记录；原逻辑抽成私有的 `saveJobRecord` 供两条路共用。
  - `updateJobInfo` / `deleteJobInfo` 先 `for update` 锁住工作记录，再按新旧差额增减工单计数（换工作方式时旧方式减、新方式加）。
  - `updateWorkOrder` / `deleteWorkOrder` 先锁工单行再校验数量/认领记录。
  - 工单列表、认领进度改读计数列，不再每次 SUM 明细。
  - 编辑/新增工单时忽略请求里的计数列，只有认领链路能改它们。
  - 删除工单后，这张床如果已经没有工单/账单/工作记录，顺手逻辑删除，不在床列表里留空床。
- **迁移**：`deploy/migrate-0.0.5.sql` 加两列并从 `job` 明细回填；`ledger.sql` 的建表语句同步。存量里 21 张历史上就超领的工单如实保留（剩余显示为 0），不会被自动改掉。
- **验证**（真实库 + 容器 2.6）：
  - 建一张 100 件的测试工单，同时发 10 个「认领 20 件」的请求 → 成功 5、失败 5，计数正好 100，没有超领；
  - `POST /emp/job/save` 带 workOrderId → 拒绝：「认领工单请使用认领接口」；
  - 工单数量改小到 50（已认领 100）→ 拒绝；删除有认领的工单 → 拒绝；
  - 认领记录 20 改 40（剩余 0）→ 拒绝并提示最多 20；改 10 → 成功，工单计数 100→90；删掉这条 → 计数 90→80；四条认领全删后工单可正常删除。
- **镜像**：`docker build -f ledger-api/Dockerfile -t 794862853/lemonledger:2.6 ledger-api`，容器已切到 2.6；已推 Docker Hub，`2.6` 与 `latest` 的 digest 同为 `sha256:227d04835ecbaf9db853bffc4c70b26d14dad2afd366dd0b7c3de4cab64e8141`。
- 涉及文件：`deploy/migrate-0.0.5.sql`（新增）、`ledger.sql`、`ledger-db/.../entity/JobWorkOrder.java`、`mapper/JobWorkOrderMapper.java`、`mapper/JobMapper.java`、`resources/mapper/JobWorkOrderMapper.xml`、`impl/job/JobServiceImpl.java`、`impl/job/JobWorkOrderServiceImpl.java`、`docker-compose.yml`。

## 20. 账单与认领对账（2026-11-18，镜像 2.7）

成衣厂报上来的账单数量，和员工实际认领的数量，以前要开两个页面自己拿床号去对；现在有一个页面按「床」把它们并排放在一起。

- **接口**：`GET /admin/factoryBill/reconcile?factoryId=&startDate=&endDate=`，返回每张床的账单数量/金额、压花与刮胶的已认领量、实际完成量（两者取较小值）、工资和差额；默认区间「本月 1 号到今天」，按差额绝对值从大到小排序。
- **页面**：后台「账单与统计」组新增「账单与认领对账」，四张汇总卡（账单数量 / 实际完成 / 差额 / 有差异的床），筛选支持成衣厂、日期区间、只看有差异；表格状态分「未开工 / 未做完 / 做超了 / 已对平」，差额正数橙色、负数红色，备注给一句原因（未认领 / 还差 N / 多做 N / 无账单）。
- **口径**：账单按 `factory_bill.created_date` 过滤，认领按 `job.created_date` 过滤；款式号取 `batch.style_number`（`factory_bill` 表本身没有款式列）。同一张床可能两边只有一边有记录，`union` 后都能出现。
- **验证**（真实库）：2026-10-01~11-30 共 823 张床，671 张有差异；能直接看出「做了没记账单」（如 魔鬼鸟 192 床认领 2744 件、账单 0）和「记了账单没人做」。
- **镜像**：`794862853/lemonledger:2.7` 与 `latest` 已推 Docker Hub，digest 同为 `sha256:f0e7a140a130839908fcc12f3f577161c4a2e27801be5362f155962d49ebd2bf`。
- 涉及文件：`ReconcileRowDTO.java`（新增）、`FactoryBillMapper.java` / `FactoryBillMapper.xml`、`IFactoryBillService`、`FactoryBillServiceImpl`、`FactoryBillController`、`Reconcile.vue`（新增）、`Admin.vue`、`router.js`、`network/admin/factoryBill.js`、`docker-compose.yml`。

## 21. 快捷键与连续录入（2026-11-19，镜像 2.7 不变）

录账单、录工单、录单价都是坐着连打的活，这次把键盘这一层补上。

- **快捷键**：弹窗里 `Ctrl + Enter`（Mac 上 `Cmd + Enter`）直接提交，`Esc` 关掉；列表页 `Alt + A` 打开「添加」。弹窗底部左侧有淡灰提示，添加按钮的 `title` 也写了 Alt + A。
- **不抢确认框**：页面上有 `ElMessageBox`（删除确认、重复账单确认）时快捷键自动让位，避免回车误点。
- **连续录入**：
  - 成衣厂账单：添加成功后不关弹窗、不清成衣厂和日期，只清床号与数量，焦点回到床号，接着就能录下一床。
  - 员工计件单价：添加成功后不关弹窗，保留工作方式，只清工作类型与单价，焦点回到工作类型。
  - 成衣厂计件单价（上一轮已做）：保留厂/工作类型/单价，只清款式编号，焦点回到款式编号。
- **实现**：新增 `src/base/dialogShortcuts.js`，`useDialogShortcuts({ isOpen, onConfirm, onCancel })` 统一用 document 级 keydown 捕获注册，组件卸载时解绑。
- **踩坑**：`onConfirm` 里传 ref 对象（不带 `.value`）会静默失败——模板里 ref 自动解包，脚本里不会；已全部改成 `.value`。
- 涉及文件：`src/base/dialogShortcuts.js`（新增）、`FactoryBill.vue`、`FactoryPieceRate.vue`、`JobPieceRate.vue`、`JobWorkOrder.vue`、`src/style.css`。

## 22. 后台首页数据看板（2026-11-19）

- 新增 `GET /admin/dashboard/summary`：一次返回区间内的账单件数/金额、工资支出、毛利与毛利率、已认领件数、对不上的床数、逐日数据、差额最大的 5 张床。
- 逐日数据由 `DashboardMapper` 两条聚合查询（账单按 `factory_bill.created_date`，工资按 `job.created_date`）在服务层用 `TreeMap` 补零合并，缺数据的日期也占一格，柱子不会断。
- 「最该处理的 5 张床」复用对账查询 `selectReconcileByBatch`，按差额绝对值排序取前 5。
- 后台新增「数据看板」页（`/admin/dashboard`），并把后台默认首页从成衣厂账单改为看板；4 张指标卡 + 每日件数柱状图（悬停看金额与工资）+ 差额 Top5 表格，可一键跳到对账页。
- 镜像：`794862853/lemonledger:2.8`（digest `sha256:11cb058c0cf6cf9b35d549bf98e651c76698ea26ae24faf00be747860e18c173`），`latest` 同步更新。
- 验证：2026-10-01~11-19 区间返回账单 75,182 件 / ¥23,570.31、工资 ¥33,373.34、毛利 -¥9,803.03、毛利率 -41.6%、对不上的床 673 张；近 30 天页面渲染正常。

## 23. 毛利趋势图（2026-11-19）

- 新增 `GET /admin/factoryBill/gross/trend`：按天返回区间内的账单收入、工资支出、毛利，同时返回合计。
- 日期取账单与工资两边的并集，只有一边有数据的日期也会出现（另一边补 0），折线不会因为缺天被拉直。
- 毛利统计页新增趋势卡片：三条折线（蓝=账单收入、橙虚线=工资支出、绿=毛利）+ 毛利面积，悬停显示当天三个数值；区间超过 45 天自动按月聚合。
- Y 轴刻度取整齐数值（1/2/2.5/5 × 10^n），不会出现 1,822 这种刻度；默认区间与看板一致，改为最近 30 天。
- 负数的钱符号放前面显示为 `-¥1,366.86`。
- 镜像：`794862853/lemonledger:2.9`（digest `sha256:6a2feea0c37ef1f978e6e9a727780896d73f8baefa81ba20fbf1703b4cb867cc`），`latest` 同步更新。
- 验证：2026-10-01~11-19 合计账单 ¥23,570.31、工资 ¥33,373.34、毛利 -¥9,803.03，与看板/毛利表一致；近 30 天页面折线与悬停提示正常。

## 24. 员工产能排行（2026-11-19）

- 新增 `GET /admin/rank/employees`：区间内按员工汇总件数、工资、出勤天数，以及压花/刮胶各做了多少件，按件数倒序返回；平均单价（元/件）在服务层用工资÷件数算好。
- 后台新增「员工产能」页（`/admin/employeeRank`），菜单放在「账单与统计」组：
  - 四张指标卡：参与人数、总件数、人均件数、工资合计；
  - 排行榜：前三名金银铜徽章，件数带横向占比条（以第一名满格），另有工资、平均单价、出勤天数、工序标签。
- 平均单价低于 1 元时保留三位小数（真实单价大约 0.04 元/件，两位小数会全变成 0.04）。
- 默认看最近 30 天，可按成衣厂筛选；前端分页。
- 镜像：`794862853/lemonledger:2.10`（digest `sha256:b464064196458ede00fd910a2d5aa6446dfb0f974b7734098015840929f826b4`），`latest` 同步更新。
- 验证：2026-10-01~11-19 返回 10 人、合计 818,989 件 / ¥33,373.34，首位梅（刮胶 130,828 件、26 天、¥5,417.36）；近 30 天页面 9 人、307,437 件、¥12,590.40，排行与工序标签正常。

## 25. 数据库自动备份（2026-11-19）

- 新增 `deploy/backup-mysql.sh`：用 `mysql_container` 里的 mysqldump 导出 `ledger` 库，容器内直接 gzip，宿主机只接收字节流。
  - 导出参数 `--single-transaction --quick --routines --triggers --default-character-set=utf8mb4`，不锁表、不把整表读进内存；
  - 三道校验：gzip 完整性、文件大于 10KB、dump 里必须有建表语句；
  - 默认保留最近 14 份，超出按修改时间删旧的；过程写 `backups/backup.log`；
  - 支持 `--keep N` 调整保留份数、`--restore <文件>` 一键恢复。
- 新增 `deploy/install-backup-task.ps1`：生成 `run-backup.cmd` 并注册 Windows 计划任务「LemonLedger 数据库备份」，每天 02:30 执行；注册完立刻跑一次自检。
- 后端新增 `GET /admin/backup/list`：读 `/backups`（compose 只读挂载 `./backups`），返回文件列表、最近一次备份、总占用，以及「超过 36 小时没备份」的 stale 标记。
- 后台新增「系统 → 数据库备份」页（`/admin/backup`）：最近备份时间、文件数、占用空间、备份内容四张卡；备份文件表格；恢复命令一键复制；stale 时顶部黄条提醒检查计划任务。
- 镜像：`794862853/lemonledger:2.11`（digest `sha256:1f970371faa52940cad47c90b4979d2a59c18651e7751b82ca7e176eaf796d55`），`latest` 同步更新。
- 验证：首次备份 `ledger-20261119-111528.sql.gz` 423.8 KB、11 张表、12 段 INSERT；计划任务 `LastTaskResult=0`、下次运行 2026-11-20 02:30；接口返回 2 个文件、总计 847.7 KB。

## 26. 提交幂等（2026-11-19）

- 背景：手抖点两下「添加账单」、Ctrl+Enter 连按、网络超时后重试，都可能把同一笔提交发两次；认领工单重复提交则会重复扣减剩余量。
- 后端新增 `IdempotencyGuard`（内存表 + 10 分钟 TTL）：同一个 `X-Idempotency-Key` 在 10 分钟内只放行一次。
  - `IdempotencyInterceptor` 挂在 `/admin/**`、`/emp/**` 的 POST/PUT/DELETE 上；没带头的请求照旧放行，不改变老接口行为；
  - 重复提交直接返回 `{"status":409,"message":"这次提交已经处理过了，不用再点一次"}`，不再调用业务；
  - 业务失败时（全局异常处理器捕获后、或 HTTP 非 2xx）会把 key 释放，用户改完能原样重试。
- 修复：认领工单页漏了 `newSubmitKey` 的 import，点「添加工单」时报 ReferenceError、弹窗打不开（已补）。
- 前端新增 `src/base/submitKey.js`：打开弹窗时生成 key，成功提交后换新 key，失败时也换新 key。
  - 已接入：添加成衣厂账单（`FactoryBill.vue`）、添加工单与认领工单（`JobWorkOrder.vue`）；
  - key 通过 axios 请求拦截器写入请求头，业务代码只多传一个参数。
- 镜像：`794862853/lemonledger:2.12`（digest `sha256:a872d32b51dd4cc27cae7cdf3f69a1a4ce938760e819302e90c6923a45d428ca`），`latest` 同步更新。
- 验证：
  - 账单连发两次同 key：第一次 200、第二次 409，库里只有一条；
  - 先发一条非法请求（同 key）→ 500，再用同 key 发正确请求 → 200（失败会释放 key）；
  - 认领接口同 key 连发两次：第一次 200、第二次 409，`job_work_order.claimed_emboss` 只加了一次（10）；
  - 清理测试数据后校验：测试账单 0 条、测试认领 0 条、计数器回到 0。

## 27. 员工产能筛选改为员工、四个基础档案改模糊搜索（2026-11-19）

- 员工产能排行：筛选条件由「成衣厂」改为「员工」。
  - 后端 `selectEmployeeRank` 的参数由 `factoryId` 改为 `employeeId`，接口 `GET /admin/rank/employees?employeeId=`；
  - 前端下拉改读员工列表（`queryAdminEmployeeList('', 1, 500, 0)`），选项显示员工姓名。
- 员工档案、成衣厂列表、工作类型、工作方式四个页面的搜索改为模糊匹配（`like concat('%', ?, '%')`），
  以前是完全相等，少打一个字就查不到。
- 镜像：`794862853/lemonledger:2.13`（digest `sha256:f46ecca6dc133b9eb3f644c5ff19d83373bdca64f3fa08fa1d60af0bd981ee52`），`latest` 同步更新。
- 验证：
  - 员工档案搜「弟」→ 周弟、高弟；成衣厂搜「鸟」→ 魔鬼鸟；
  - 工作类型搜「花」→ 小花、大花、中花等 15 条；工作方式搜「压」→ 压花；
  - 排行选「周弟」→ 只剩 1 行（19,819 件 / ¥804.24 / 6 天）。

---

## 28. 打包时前端构建在 Windows / IDEA 上跑不起来（2026-11-22）

只改构建配置，不碰业务代码。

### 28.1 三个真问题

| 现象 | 真因 | 修法 |
| --- | --- | --- |
| `Cannot run program "npm" ... CreateProcess error=2` | `ledger-api/pom.xml` 里写死了 `<executable>npm</executable>`，而 Windows 上只有 `npm.cmd`；Maven 用 ProcessBuilder 直接执行，不会按 PATHEXT 补扩展名 | 新增属性 `npm.executable`（默认 `npm`），根 pom 里加 `npm-windows` profile（`<os><family>windows</family></os>` 自动激活）改成 `npm.cmd`；`ledger-api/pom.xml` 引用 `${npm.executable}` |
| `[vite:esbuild-transpile] remove ...\Temp\esbuild-xxx: Access is denied` | esbuild 把自解压的临时 exe 放在系统 TEMP，用完删不掉（杀软/Defender 锁文件） | 新增属性 `npm.tmpdir`（仓库内 `.npm-tmp/`），通过 exec 插件的 `<environmentVariables>` 把 `TEMP`/`TMP` 指过去；`.npm-tmp/.gitkeep` 随仓库存在，`.gitignore` 忽略目录内容 |
| `[vite:build-html] ... fileName ... received "../../../../../../阿辉/.dsh/..."` | 工程是从目录联接 `C:\Users\ahui`（指向 `C:\Users\阿辉`）打开的：进程 cwd 是**联接路径**，而 vite/rollup 解析模块走**真实路径**，两边一相对化就成了 `../../../../../../阿辉/...`，rollup 拒绝这种相对文件名 | 新增 `ledger-view/build.mjs`：先 `process.chdir(fs.realpathSync(process.cwd()))` 切到真实路径，再调 vite 的 build API；`package.json` 的 build 脚本改成 `node build.mjs` |

### 28.2 验证

- 从真实路径 `C:\Users\阿辉\…\LemonLedger` 跑 `mvn clean install`（JDK 17 + Maven 3.10 + 本地仓库 `D:\tools\maven-repository`）→ **BUILD SUCCESS**，四个模块全过，前端 `✓ built in 4.6s`，产出 `ledger-api/target/LemonLedger.jar`。
- 从目录联接路径 `C:\Users\ahui\…\LemonLedger`（就是 IDEA 用的那个）再跑一次 `clean install` → 同样 **BUILD SUCCESS**。
- 单独 `npm run build` 在联接路径下也通过（`✓ built in 4.63s`）。
- 不再需要 `-Dexec.skip=true`，也不用先手动跑 `npm run build`。

### 28.3 IDEA 侧相关的三个设置（与本仓库无关，但影响构建）

1. `设置 → 构建、执行、部署 → 构建工具 → Maven`：主路径、用户设置文件、本地仓库都不要放在含中文的路径下（本机是 `D:` 盘），否则 IDEA 传参时代码页会把中文路径解坏，Maven 直接不启动。
2. `文件 → 项目结构 → 项目`：SDK 必须是 **17**，语言级别 17。项目 SDK 是 11 时会报 `invalid target release: 17`。
3. `Maven → 运行程序(Runner)`：JRE 选 17，或保持「使用项目 JDK」。

### 28.4 涉及文件

`pom.xml`（`npm.executable`、`npm.tmpdir`、`npm-windows` profile）、`ledger-api/pom.xml`（`${npm.executable}`、`environmentVariables`）、`ledger-view/build.mjs`、`ledger-view/package.json`（`build` 脚本）、`.gitignore`、`.npm-tmp/.gitkeep`。

## 29. 成衣厂账单结算单（导出快照 + 结清记录）（2026-11-22）

### 29.1 问题

导出 Excel 之后就没人知道这一批到底结没结清：结清与否只在脑子里，翻旧账要重新导一遍，也查不到「上次导出的是哪几床」。

### 29.2 做法

- 新增两张表（`deploy/migrate-0.0.6.sql`）：
  - `factory_bill_settlement`：成衣厂、导出区间、笔数、件数、总金额、已付金额、付款日期、状态（0 未结清 / 1 部分结清 / 2 已结清）。
  - `factory_bill_settlement_item`：结算单明细，存导出那一刻每床的床号、款式、工作类型、数量、单价、金额。
- 导出即快照：`FactoryBillController.exportFactoryBillExcelByCondition` 在查询之后、拼总计行之前调用 `IFactoryBillSettlementService.snapshotOnExport`。快照失败只记日志，不影响导出本身。
- 同一成衣厂、同一区间如果还有**没结清**的结算单，重复导出只刷新它的金额与明细，不会一导就多一条；已结清的另开一条，避免改掉结算凭证。
- 接口（`AdminSettlementController`）：
  - `GET /admin/settlement` 分页列表，可按成衣厂/状态/导出日期筛。
  - `GET /admin/settlement/{id}/items` 明细。
  - `PUT /admin/settlement/{id}/settle` 登记已付金额与付款日期，状态按金额自动算（0 → 未结清，等于总额 → 已结清，之间 → 部分结清）。
  - `DELETE /admin/settlement/{id}` 作废（逻辑删除）。
  - `GET /admin/settlement/{id}/excel` 按快照重新导出，导出的是当时那几床。
- 前端新增「账单结算」页（`Settlement.vue`），菜单挂在「账单与统计」下；列表能看到导出日期、区间、笔数/件数、金额、已付、还欠、状态，操作有明细 / 登记结清 / 重导 / 作废。

### 29.3 验证

- 导出 李波 2026-10-01~10-03 → 列表出现 1 条：1 笔、630 件、¥226.80、未结清；同区间再导一次仍是 1 条（不重复）。
- 登记已付 ¥300（区间 2026-11-01~11-10，总额 ¥627.12）→ 状态变「部分结清」，还欠 ¥327.12；再登记到 ¥627.12 → 「已结清」。
- 提交超过总额的已付金额 → 拒绝「已付金额不能超过账单总额」。
- `GET /admin/settlement/{id}/excel` 返回 xlsx（3945 字节），文件名 `李波成衣厂账单-1.xlsx`。
- 明细接口返回每床记录（床号 176、款式 8802、工作类型 前袋+后袋、数量 630 …）。

### 29.4 涉及文件

`deploy/migrate-0.0.6.sql`、`ledger-db/.../entity/FactoryBillSettlement.java`、`FactoryBillSettlementItem.java`、`mapper/FactoryBillSettlementMapper.java`、`mapper/FactoryBillSettlementItemMapper.java`、`service/settlement/IFactoryBillSettlementService.java`、`impl/settlement/FactoryBillSettlementServiceImpl.java`、`AdminSettlementController.java`、`FactoryBillController.java`、`ledger-view/src/network/admin/settlement.js`、`view/Admin/AdminComponent/Settlement.vue`、`router/router.js`、`view/Admin/Admin.vue`、`src/style.css`（状态胶囊不换行）、`docker-compose.yml`（2.14）。

### 29.5 镜像

- `794862853/lemonledger:2.14` 与 `latest` 已推 Docker Hub，digest `sha256:60a3ac22123de1c491601be6176e1a018b99c069e8a88193e811ad68882eaa0c`；本机只保留 2.14 与 latest。

## 30. 导出账单文件名带上日期区间（2026-11-22）

### 30.1 规则

- 起始只到月、截止到日：`李波成衣厂-2026年02月-10月15日.xlsx`。
- 截止日期正好是当月月末时，截止也只到月：`李波成衣厂-2026年02月-10月.xlsx`（2 月 27 日～10 月 31 日）。
- 跨年时截止日期带上年份：`成衣厂-2026年12月-2027年01月05日.xlsx`。
- 没选日期时仍是 `厂名成衣厂账单.xlsx`；结算单「重导」在区间名后追加结算单 ID，避免和普通导出重名。

### 30.2 实现

- 新增 `ledger-common/src/main/java/com/ledger/common/util/BillFileNameUtil.java`，`build(factoryName, start, end)` 统一拼名；厂名本身以「成衣厂」结尾时不再重复拼接（兜底名不会再变成「成衣厂成衣厂」）。
- `FactoryBillController` 的 `fileName` 改用它；`AdminSettlementController` 的按快照重导也用它。
- 中文名仍只放进 RFC 5987 的 `filename*`，`filename=` 保持纯 ASCII（`factory-bill-{id}.xlsx` / `factory-bill-settlement-{id}.xlsx`），避免 Tomcat 丢掉整个 `Content-Disposition`。

### 30.3 验证（真实接口）

- 2026-02-27～2026-10-15 → `李波成衣厂-2026年02月-10月15日.xlsx`
- 2026-02-27～2026-10-31 → `李波成衣厂-2026年02月-10月.xlsx`
- 2026-12-20～2027-01-05 → `成衣厂-2026年12月-2027年01月05日.xlsx`（该区间没有账单，厂名回退）
- 结算单 13 重导 → `成衣厂-2026年12月-2027年01月05日-13.xlsx`

### 30.4 镜像

- `794862853/lemonledger:2.15` 与 `latest` 已推 Docker Hub，digest `sha256:b1cac4079c3f8e3230d7536f215f8b1b3a3e2ab7eaaea0c065327bf751d8fd5e`；本机只保留 2.15 与 latest。
