<#
.SYNOPSIS
    LemonLedger 数据库备份脚本（MySQL，Windows）

.DESCRIPTION
    把 ledger 库整个导出来，压缩成一个 .sql.gz 放的数据备份目录，并自动删除
    超过保留天数的旧备份。默认优先走 docker（MySQL 跑在容器里时用），
    找不到容器就退回本机的 mysqldump。

    原来的仓库里没有任何备份手段，docker-compose 里 mysql 那段还是注释掉的，
    真实账目在宿主机上只有一份。脚本不大，但出事的时候它就是救命的那份。

.EXAMPLE
    # 手动跑一次
    powershell -ExecutionPolicy Bypass -File .\deploy\backup-mysql.ps1

.EXAMPLE
    # 备份到 D 盘的目录，保留 30 天，密码从环境变量取
    $env:LEDGER_DB_PASSWORD = "你的密码"
    powershell -ExecutionPolicy Bypass -File .\deploy\backup-mysql.ps1 -BackupDir D:\LedgerBackup -KeepDays 30

.NOTES
    建议用 Windows 任务计划程序每天跑一次（触发器：每天 02:00）。
#>

[CmdletBinding()]
param(
    # 备份文件存放目录
    [string] $BackupDir = (Join-Path $PSScriptRoot 'backup'),

    # 保留多少天，超过的会被删掉
    [int] $KeepDays = 14,

    # 数据库连接信息
    [string] $DbHost = '127.0.0.1',
    [int]    $DbPort = 3306,
    [string] $DbName = 'ledger',
    [string] $DbUser = 'root',

    # 密码优先取参数，其次取环境变量 LEDGER_DB_PASSWORD，都没有就用 root
    [string] $DbPassword = $(if ($env:LEDGER_DB_PASSWORD) { $env:LEDGER_DB_PASSWORD } else { 'root' }),

    # MySQL 容器名；留空则自动查找名字里带 mysql 的容器
    [string] $ContainerName = ''
)

$ErrorActionPreference = 'Stop'

# ---------- 准备目录 ----------
if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
}

$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$fileName  = "ledger-$timestamp.sql"
$backupFile = Join-Path $BackupDir $fileName

# ---------- 选择备份方式：docker 优先，其次本机 mysqldump ----------
$dockerAvailable = $null -ne (Get-Command docker -ErrorAction SilentlyContinue)

if ($dockerAvailable -and [string]::IsNullOrWhiteSpace($ContainerName)) {
    $candidates = @(docker ps --filter 'name=mysql' --format '{{.Names}}' 2>$null)
    if ($candidates.Count -gt 0) {
        $ContainerName = $candidates[0]
    }
}

if ($dockerAvailable -and -not [string]::IsNullOrWhiteSpace($ContainerName)) {
    Write-Host "使用 docker 容器 [$ContainerName] 备份 $DbName ..."
    # 用 cmd 做重定向：PowerShell 的 > 会按文本重新编码，导出的 .sql 可能变成 UTF-16 而无法导入
    # --single-transaction：不锁表，业务照常跑；--routines --triggers：存储过程和触发器一起导
    $cmd = 'docker exec {0} mysqldump -u {1} --password={2} --single-transaction --routines --triggers --default-character-set=utf8mb4 {3} > "{4}"' -f `
        $ContainerName, $DbUser, $DbPassword, $DbName, $backupFile
    & cmd /c $cmd
    if ($LASTEXITCODE -ne 0) {
        throw "docker 备份失败（退出码 $LASTEXITCODE），请检查容器名、账号密码和库名。"
    }
}
elseif (Get-Command mysqldump -ErrorAction SilentlyContinue) {
    Write-Host "使用本机 mysqldump 备份 $DbName ..."
    $cmd = 'mysqldump -h {0} -P {1} -u {2} --password={3} --single-transaction --routines --triggers --default-character-set=utf8mb4 {4} > "{5}"' -f `
        $DbHost, $DbPort, $DbUser, $DbPassword, $DbName, $backupFile
    & cmd /c $cmd
    if ($LASTEXITCODE -ne 0) {
        throw "mysqldump 备份失败（退出码 $LASTEXITCODE），请检查账号密码和库名。"
    }
}
else {
    $hint = if (-not $dockerAvailable) {
        'docker 命令不在 PATH 里（Docker Desktop 没启动，或没把 C:\Program Files\Docker\Docker\resources\bin 加进 PATH）'
    } else {
        '有 docker，但没找到名字带 mysql 的容器，也没装本机 mysqldump'
    }
    throw ('备份失败：' + $hint + '。可以先 docker ps 看容器名，再用 -ContainerName 指定，例如：.\deploy\backup-mysql.ps1 -ContainerName lemonledger-mysql')
}

# ---------- 校验 ----------
if (-not (Test-Path $backupFile)) {
    throw "备份失败：没有生成文件 $backupFile"
}

$size = (Get-Item $backupFile).Length
if ($size -lt 1024) {
    throw "备份文件只有 $size 字节，明显不对（很可能密码错或库不存在）。请手工确认：$backupFile"
}

Write-Host ("备份完成：{0}（{1:N1} KB）" -f $backupFile, ($size / 1KB))

# ---------- 清理过期备份 ----------
$limit = (Get-Date).AddDays(-$KeepDays)
$expired = Get-ChildItem -Path $BackupDir -Filter 'ledger-*.sql' | Where-Object { $_.LastWriteTime -lt $limit }

foreach ($file in $expired) {
    Remove-Item $file.FullName -Force
    Write-Host "已删除过期备份：$($file.Name)"
}

Write-Host "当前保留 $((Get-ChildItem -Path $BackupDir -Filter 'ledger-*.sql').Count) 份备份（保留 $KeepDays 天）。"
