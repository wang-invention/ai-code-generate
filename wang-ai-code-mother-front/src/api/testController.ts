// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /test/get */
export async function test(options?: { [key: string]: any }) {
  return request<string>('/test/get', {
    method: 'GET',
    ...(options || {}),
  })
}
