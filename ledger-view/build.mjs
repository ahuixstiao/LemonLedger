/**
 * 前端构建入口。
 *
 * 为什么不用 `vite build` 直接当脚本：
 * 本机的 C:\Users\ahui 是 C:\Users\阿辉 的目录联接（junction，用来绕开中文路径）。
 * IDE / Maven 从联接路径启动进程时，process.cwd() 拿到的是联接路径，
 * 而 vite、rollup 在解析模块时会走真实路径，两套路径一相对化就成了
 *   ../../../../../../阿辉/.dsh/.../ledger-view/index.html
 * rollup 会直接报 “fileName must be a string that is neither absolute nor relative”。
 *
 * 所以先把工作目录切到真实路径，再调 vite 的 build API。
 */
import fs from 'node:fs'

try {
  const real = fs.realpathSync(process.cwd())
  if (real !== process.cwd()) {
    process.chdir(real)
  }
} catch {
  // 拿不到真实路径就按原样继续，交给 vite 自己处理
}

const { build } = await import('vite')

await build()
