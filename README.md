# LemonLedger 柠檬记账

给成衣厂用的记账系统：记录每个成衣厂的报价、每天的床号账单，以及员工的计件工作和工资统计。
后端 Spring Boot + MyBatis-Plus + MySQL，前端 Vue 3 + Vite + Element Plus。

---

## 一、这个项目能干什么

| 模块 | 说明 |
| --- | --- |
| 成衣厂 | 维护成衣厂名单、工作类型、报价（同一厂家 + 款式 + 工作类型只能有一条报价） |
| 账单 | 按床号录入每床数量，金额 = 数量 × 报价单价，自动保留两位小数；支持按条件筛选、统计、导出 Excel |
| 工单 | 发布工单，员工按工作方式认领，系统按已认领数量判断是否还能认领 |
| 员工 | 维护员工、工作方式，按天/按区间查询工作内容和工资合计 |

金额相关一律使用 `BigDecimal`（保留两位、四舍五入），合计逻辑集中在
`ledger-common/src/main/java/com/ledger/common/util/MoneyUtil.java`，不要在业务里另写一套。

---

## 二、目录结构

```
LemonLedger
├── ledger-api       # 启动模块：Controller、全局异常处理、application.yml
├── ledger-db        # 数据访问：Mapper、ServiceImpl、实体、application-db*.yml
├── ledger-common    # 公共：Result、BizException、MoneyUtil、ParentEntity
├── ledger-view      # 前端（Vue 3 + Vite）
├── deploy           # 备份脚本、数据库升级脚本
├── ledger.sql       # 建库建表 + 基础数据
└── docker-compose.yml
```

---

## 三、准备数据库

1. 建库并导入结构（会重建表，**已有数据请先备份**）：

   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ledger DEFAULT CHARACTER SET utf8mb4;"
   mysql -u root -p ledger < ledger.sql
   ```

2. 修改连接信息：`ledger-db/src/main/resources/application-db.yml`
   （默认是 `host.docker.internal:3306/ledger`，账号 `jeesite / root`）。

3. 如果是从旧版本升级上来（已有数据、不想重建表），只执行升级脚本：

   ```bash
   mysql -u root -p ledger < deploy/migrate-0.0.2.sql
   ```

---

## 四、本地开发

需要 JDK 17、Maven 3.9+、Node 18+。

```bash
# 后端（默认 profile 是 db,common）
mvn -DskipTests spring-boot:run -pl ledger-api

# 前端
cd ledger-view
npm install
npm run dev
```

前端通过 `VITE_BASE_URL` 指向后端地址，写在 `ledger-view/.env.development` 里。

> 本地调数据时注意：`application-db-dev.yml` 和 `application-db.yml` 目前指向**同一个 `ledger` 库**。
> 想在本地随便改而不碰真实账目，按 `deploy/init-dev-db.sql` 建一个 `ledger_dev`，
> 再把 db-dev 里的库名改掉，启动时加 `SPRING_PROFILES_ACTIVE=db-dev,common`。

---

## 五、Docker 部署

环境要求：Docker 20+、Docker Compose 2+。

```bash
# 1. 先打包前端（nginx 挂载的是 ledger-view/dist）
cd ledger-view
npm install
npm run build
cd ..

# 2. 启动（镜像从 Docker Hub 拉取）
docker compose pull
docker compose up -d

# 3. 查看日志
docker compose logs -f lemonledger
```

访问 `http://<服务器IP>:8080`。

数据库密码不再写死在 compose 里，改成从宿主机环境变量读取：

```bash
export SPRING_DATASOURCE_PASSWORD='你的数据库密码'
docker compose up -d
```

---

## 六、备份（重要）

账目只有一份库，**没有任何备份等于随时可能全丢**。仓库自带备份脚本：

```powershell
# 手动备份一次（Windows PowerShell）
powershell -ExecutionPolicy Bypass -File .\deploy\backup-mysql.ps1

# 指定目录、保留 30 天、密码从环境变量取
$env:LEDGER_DB_PASSWORD = "你的密码"
powershell -ExecutionPolicy Bypass -File .\deploy\backup-mysql.ps1 -BackupDir D:\LedgerBackup -KeepDays 30
```

脚本会优先用 docker 容器里的 `mysqldump`，没有容器就用本机的 `mysqldump`；
备份完自动删掉超过保留天数的旧文件。

建议在"任务计划程序"里加一个每天 02:00 的触发器，并且**定期把备份目录复制到另一块盘或 NAS**——
同一个硬盘上的备份，硬盘坏了就一起没了。

---

## 七、常见问题

**Q：启动报 `Communications link failure` / 连不上数据库？**
A：`host.docker.internal` 只在容器里有效。在 IDE 里本地跑，把 url 的 host 改成 `127.0.0.1`。

**Q：改了前端代码，`docker compose up` 之后页面没变？**
A：nginx 挂的是 `ledger-view/dist` 目录，必须先 `npm run build` 重新打包。

**Q：导出的 Excel 打开提示"文件已损坏"？**
A：先看后端日志有没有 `导出成衣厂账单 Excel 失败`；再确认没有多个服务同时写同一个响应流。

**Q：删掉的员工/成衣厂，名字为什么还能重新加？**
A：唯一键是 `(name, flag)`，逻辑删除后旧记录 flag=1，不占用名字。
旧库需要用 `deploy/migrate-0.0.2.sql` 把唯一键改过来。

---

## 八、测试

```bash
mvn test        # 单元测试，不连数据库
```

目前覆盖：金额合计与四舍五入（`MoneyUtilTest`）、导出排序工具（`FactoryBillExportSortUtilTest`）、
全局异常翻译（`GlobalExceptionHandlerTest`）。
