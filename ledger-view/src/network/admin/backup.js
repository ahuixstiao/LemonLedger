import { request } from '../../base/request.js'

/**
 * 数据库备份文件列表。
 * 备份本身由宿主机定时脚本执行，这里只负责查看。
 */
export function queryBackupList() {
  return request({
    url: '/admin/backup/list',
    method: 'get'
  })
}
