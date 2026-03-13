// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /crawl/logs */
export async function listLogs(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listLogsParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageCrawlTaskLog>('/crawl/logs', {
    method: 'GET',
    params: {
      ...params,
      queryRequest: undefined,
      ...params['queryRequest'],
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /crawl/trigger */
export async function triggerCrawl(options?: { [key: string]: any }) {
  return request<API.BaseResponseCrawlTaskLog>('/crawl/trigger', {
    method: 'POST',
    ...(options || {}),
  })
}
