// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /work/flow/execute */
export async function executeWorkflow(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.executeWorkflowParams,
  options?: { [key: string]: any }
) {
  return request<API.WorkflowContext>('/work/flow/execute', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
