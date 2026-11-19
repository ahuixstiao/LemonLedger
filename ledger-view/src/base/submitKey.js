/**
 * 提交幂等用的 key：打开弹窗时生成一个，提交时带上；
 * 后端同一个 key 只处理一次，手抖点两下或网络重试都不会产生两条记录。
 */
export function newSubmitKey() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}
