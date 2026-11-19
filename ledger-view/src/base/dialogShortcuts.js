import { onBeforeUnmount, onMounted } from 'vue'

/**
 * 弹窗快捷键：Ctrl/⌘ + Enter 提交，Esc 关闭。
 *
 * 为什么不直接绑在 el-input 的 @keyup.enter 上：
 * - 弹窗里有 select、日期选择器，焦点不一定在输入框上；
 * - 录制账单时经常一只手拿单子、一只手敲键盘，焦点跑到哪都要能提交。
 *
 * 只在这个弹窗打开时生效；如果上面盖着确认框，就不抢键。
 */
export function useDialogShortcuts({ isOpen, onConfirm, onCancel }) {
  const handler = event => {
    if (!isOpen()) return
    // 有确认框 / 下拉菜单打开时让给它们
    if (document.querySelector('.el-message-box')) return
    const key = event.key
    const isSubmit = (event.ctrlKey || event.metaKey) && (key === 'Enter' || key === 'NumpadEnter')
    if (isSubmit) {
      event.preventDefault()
      onConfirm()
      return
    }
    if (key === 'Escape') {
      event.preventDefault()
      onCancel()
    }
  }

  onMounted(() => document.addEventListener('keydown', handler, true))
  onBeforeUnmount(() => document.removeEventListener('keydown', handler, true))
}
