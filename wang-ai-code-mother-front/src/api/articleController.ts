// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /articles/detail/${param0} */
export async function getArticleDetail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getArticleDetailParams,
  options?: { [key: string]: any }
) {
  const { articleId: param0, ...queryParams } = params
  return request<API.BaseResponseArticleDetailVO>(`/articles/detail/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /articles/list */
export async function listArticles(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listArticlesParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseArticlePageVO>('/articles/list', {
    method: 'GET',
    params: {
      ...params,
      queryRequest: undefined,
      ...params['queryRequest'],
    },
    ...(options || {}),
  })
}
