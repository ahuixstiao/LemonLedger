# LemonLedger 升级操作清单（照着做就行）

> **状态（2026-11-07）：这份清单里的 0.0.2 / 0.0.3 / 0.0.4 已经在真实库上执行完了**，备份在 `deploy/backup/ledger-2026-11-07-before-migrations.sql`，过程记录见 `CHANGES.md` 第 10 节。
> 现在这份清单的用途变成两件：① 换机器/重建环境时照着走；② 出问题时对照着查。
> 已经执行过的库不要重复跑迁移（第 3 步会报「表不存在」，那是正常的）。

> 这份是给你自己在**连着真实数据的机器上**执行用的。
> 代码、迁移脚本、补丁都已经在仓库里了；下面每一步都写了「怎么确认它成功了」。
> 全程大概 15–25 分钟。

---

## 0. 先确认你在哪

```powershell
cd C:\Users\阿辉\.dsh\profiles\desktop\LemonLedger
git log --oneline -1
```

应该看到 `docs: 0.0.4 迁移补上表/列改名与工作类型字典合并步骤，CHANGES 同步`（或更新的提交）。
如果看不到，说明你打开的不是改过的那个目录。

> 补丁文件也在根目录：`fix-LemonLedger.patch`，基线是 `f4556a0`；改了什么看 `git log --oneline f4556a0..HEAD`。

---

## 0.5 确认 docker 和 MySQL 容器名（你的 MySQL 跑在容器里）

```powershell
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Ports}}"
```

- 找到那个 MySQL 容器，记下 **第一列的容器名**（比如 `lemonledger-mysql`、`mysql`、`mysql8`）。
  下文所有 `<容器名>` 都换成它。
- 如果 `docker` 这个命令本身都找不到（我在这台机器的 PATH 里没看到），先加：
  ```powershell
  $env:Path += ";C:\Program Files\Docker\Docker\resources\bin"
  ```
  或者用 Docker Desktop 的终端。
- 忘了 root 密码就看 compose 文件里的 `MYSQL_ROOT_PASSWORD`，或者：
  ```powershell
  docker exec -it <容器名> printenv MYSQL_ROOT_PASSWORD
  ```

> 本机没有 `mysql.exe` 也无所谓了——命令都在容器里执行，宿主机不需要装客户端。

### 关于密码的一个坑（很重要）

**`docker exec` 后面用 `-p` 不带密码、又重定向了 SQL 文件时，mysql 会把 SQL 文件当密码读**，直接报错或干脆不执行。
所以两种写法选一个：

- 密码写进命令（会留在历史里，自己的机器问题不大）：
  ```powershell
  cmd /c "docker exec -i <容器名> mysql -uroot -p你的密码 ledger < deploy\migrate-0.0.2.sql"
  ```
- 或者用环境变量，避免明文：
  ```powershell
  cmd /c "docker exec -i -e MYSQL_PWD=你的密码 <容器名> mysql -uroot ledger < deploy\migrate-0.0.2.sql"
  ```


---

## 1. 备份（别跳）

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\backup-mysql.ps1
```

- 脚本会自己找 docker 容器（按名字里带 `mysql` 找），在容器里跑 `mysqldump`，再把 sql 拉到 `deploy\backup\`。
- **容器名里不带 mysql**（比如叫 `lemonledger-db`）就显式传进去：
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\deploy\backup-mysql.ps1 -ContainerName <容器名>
  ```
- **确认**：`deploy\backup\` 里出现 `ledger-日期.sql`，大小不是 0。
- 脚本报「找不到 mysqldump / docker」时，多半是 PATH 里没有 docker：
  ```powershell
  $env:Path += ";C:\Program Files\Docker\Docker\resources\bin"
  ```
- 不想用脚本就手动来（效果一样，容器里 dump 到 stdout，宿主机重定向成文件）：
  ```powershell
  cmd /c "docker exec -i <容器名> mysqldump -uroot -p你的密码 --single-transaction --routines --triggers --default-character-set=utf8mb4 ledger > deploy\backup\ledger-manual.sql"
  ```

---

## 2. 停掉正在跑的后端

把 `java -jar` 的窗口关掉，或 IDE 里点停止。**迁移期间别让服务写库。**

---

## 3. 按顺序跑三个迁移脚本

⚠ 顺序不能换：`0.0.2` → `0.0.3` → `0.0.4`，一个跑完再跑下一个。

PowerShell 里 `... < 文件` 这种写法**不生效**（`<` 是保留字符），统一套一层 `cmd /c "..."`。
数据库在容器里，所以命令是 `docker exec -i`（`-i` 别丢，否则 SQL 文件传不进去）。

先 `cd` 到项目根目录：

```powershell
cd C:\Users\阿辉\.dsh\profiles\desktop\LemonLedger
```

### 3.1 `migrate-0.0.2.sql` —— 唯一键带上 flag

```powershell
cmd /c "docker exec -i <容器名> mysql -uroot -p你的密码 ledger < deploy\migrate-0.0.2.sql"
```

让「删掉的员工/成衣厂，名字还能再用一次」。

**验证**：

```powershell
docker exec -it <容器名> mysql -uroot -p你的密码 ledger -e "SHOW INDEX FROM employee WHERE Key_name='unique_name'; SHOW INDEX FROM factory WHERE Key_name='unique_factory_name';"
```

应该各有两行（`name` + `flag` / `factory_name` + `flag`）。

### 3.2 `migrate-0.0.3.sql` —— 索引 / updated_date / mode_id 放开

```powershell
cmd /c "docker exec -i <容器名> mysql -uroot -p你的密码 ledger < deploy\migrate-0.0.3.sql"
```

**验证**：

```powershell
docker exec -it <容器名> mysql -uroot -p你的密码 ledger -e "SHOW INDEX FROM job WHERE Key_name LIKE 'job_%'; SHOW COLUMNS FROM employee LIKE 'mode_id'; SHOW COLUMNS FROM employee LIKE 'updated_date';"
```

（`mode_id` 的 Null 列应为 YES，`updated_date` 应该有这一列）

### 3.3 `migrate-0.0.4.sql` —— 改名 + 字典合并 + 建床（**只能跑一次**）

```powershell
cmd /c "docker exec -i <容器名> mysql -uroot -p你的密码 ledger < deploy\migrate-0.0.4.sql"
```

这一步做了四件事：
1. 表名/列名改成新名字：`factory_piece_rate`、`job_piece_rate`、`price`、`piece_rate_id`；
2. 工作类型字典合并：旧表改名成 `factory_job_category_bak`（**没删**），缺的名字补进 `job_category`，成衣厂计件单价上的 `category_id` 按名字重新对到新字典；
3. 建 `batch`（床）表；
4. 三张单据各加 `batch_id`，从现有数据反推床并回填。

**重复执行会在第 0 步报「表不存在 / 列不存在」**——那是已经跑过的正常信号，别慌，也别再跑。

**验证 A（工作类型没对上）**，期望 **0 行**：

```powershell
docker exec -it <容器名> mysql -uroot -p你的密码 ledger -e "SELECT p.id,p.style_number,p.category_id FROM factory_piece_rate p LEFT JOIN job_category j ON j.id=p.category_id AND j.flag=0 WHERE p.flag=0 AND j.id IS NULL;"
```

有行的话：把那些 `category_id` 手工指到正确的工作类型，再往下走。

**验证 B（单据没挂上床）**，期望三行都是 **0**：

```powershell
docker exec -it <容器名> mysql -uroot -p你的密码 ledger -e "SELECT 'job' tbl,COUNT(*) c FROM job WHERE batch_id IS NULL AND flag=0 UNION ALL SELECT 'work_order',COUNT(*) FROM job_work_order WHERE batch_id IS NULL AND flag=0 UNION ALL SELECT 'bill',COUNT(*) FROM factory_bill WHERE batch_id IS NULL AND flag=0;"
```

`bill` 那行不为 0，多半是它指向的计件单价已经被删了（孤儿账单）——先把数据修好，别急着开服务。

**看一眼合并结果**（可选）：

```powershell
docker exec -it <容器名> mysql -uroot -p你的密码 ledger -e "SELECT COUNT(*) FROM batch WHERE flag=0; SELECT category FROM job_category WHERE flag=0 ORDER BY id;"
```

---

## 4. 重新构建前端

```powershell
cd C:\Users\阿辉\.dsh\profiles\desktop\LemonLedger\ledger-view
npm run build
```

**确认**：最后一行是 `✓ built in ...`，`dist\` 目录时间戳是刚刚。

> 我这边第一次构建失败是临时目录权限（esbuild 在 `AppData\Local\Temp` 被拒），把 `$env:TEMP` 指到项目里就好了。你本地一般不会遇到。

---

## 5. 起后端

IDE 里跑 `LedgerApplication`，或者：

```powershell
java -jar ledger-api\target\LemonLedger.jar
```

- 确认启动日志里 profile 是 **`db,common`**（不是 `db-dev`）。
- 确认端口 8080 起来了，浏览器打开 `http://localhost:8080` 能进页面。

---

## 6. 点一遍（5 分钟冒烟）

| 页面 | 做什么 | 应该看到 |
| --- | --- | --- |
| 账单 | 新增一条 | 能保存；`SELECT * FROM batch ORDER BY id DESC LIMIT 1` 能看到自动建的床 |
| 账单 | 改床号 | 保存后 `job` / `job_work_order` 里同床的床号跟着变 |
| 账单 | 同一天重复记同一笔 | 弹确认框，点「继续」能强存（返工/补做） |
| 工单 | 新增 | 列表多出「已认领」「剩余」两列 |
| 认领 | 选一个员工认领 | 弹窗显示「工单数量 X，已认领 Y，还可认领 Z」；员工没设默认工作方式时会要求选 |
| 后台 | 工作类型 | 只有一份字典；原来的「成衣厂工作类型」菜单没了（预期） |
| 后台 | 毛利统计 | 选日期区间，能出「账单收入 / 工资支出 / 毛利」和明细 |

---

## 7. 出问题了怎么办

1. **先看后端日志**的最后 30 行，把报错原文发我（带堆栈最好）。
2. 代码回滚：`git revert` 或 `git reset --hard f4556a0`（⚠ 基线里没有 yml 配置文件，reset 会把它们删掉，先 `git stash -u` 或手工把 `application*.yml` 拷出来）。
3. 数据库回滚：`CHANGES.md` 第 6 节有反向 SQL；`factory_job_category_bak` 还在，改名回去就能恢复旧字典。
4. 备份文件在 `deploy\backup\`，最坏情况下整库恢复。

---

## 7.5 常见报错速查（先看这里，再发我）

| 报错 | 意思 | 怎么办 |
| --- | --- | --- |
| `Duplicate entry '张三-0' for key 'employee.unique_name'`（0.0.2 时） | 库里已经有同名且都没删的员工 | 先查 `SELECT name, COUNT(*) FROM employee WHERE flag = 0 GROUP BY name HAVING COUNT(*) > 1;`，把多余那条删掉或改成 `flag = 1`，再重跑 0.0.2（`factory` 同理） |
| `Duplicate key name 'xxx'`（0.0.3 时） | 这个索引已经有了（多半是跑过） | 把那条 `ADD INDEX` 注释掉再跑剩下的 |
| `Table 'ledger.factory_quotation' doesn't exist`（0.0.4 第 0 步） | 已经跑过 0.0.4，或者库里本来就是新表名 | 别重跑；直接去看第 3.3 节那两条验证 |
| `Unknown column 'updated_date'`（0.0.4 或启动时） | 0.0.3 没跑 | 先跑 0.0.3，再跑 0.0.4 |
| 启动报 `Unknown column 'piece_rate_id'` / `Table 'ledger.job_piece_rate' doesn't exist` | 库还没改名，0.0.4 没跑成 | 跑 0.0.4 第 0 步，或整份 0.0.4 重来（先确认旧表名还在） |
| 启动报 `Table 'ledger.batch' doesn't exist` | 0.0.4 只跑了一半 | 从 0.0.4 第 1 节往后接着跑 |

---

## 8. 还没人验证的那部分（说清楚）

- 我这边**没有 MySQL、没有 Docker**，服务没真跑起来过；编译、15 个单测、前端构建都是真跑通的。
- 所以第 6 节那张表就是你替我做的那部分验证——**哪一行不对，直接告诉我，我改。**
- B 站那套工具（`bili_scan`）在这个会话里没挂上，重开一个会话就回来。
