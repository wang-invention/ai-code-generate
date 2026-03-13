declare namespace API {
  type AppAddRequest = {
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: number
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
  }

  type AppUpdateRequest = {
    id?: number
    appName?: string
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    isHistory?: boolean
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type ArticleDetailVO = {
    articleId?: number
    title?: string
    sourceUrl?: string
    sourcePlatform?: string
    author?: string
    publishTime?: string
    content?: string
    category?: string
    isFeatured?: number
    crawlTime?: string
    status?: string
    uniqueKey?: string
    views?: number
  }

  type ArticleListVO = {
    articleId?: number
    id?: number
    title?: string
    sourcePlatform?: string
    sourceName?: string
    sourceUrl?: string
    originalUrl?: string
    author?: string
    publishTime?: string
    category?: string
    isFeatured?: number
    views?: number
    status?: string
    summary?: string
  }

  type ArticlePageVO = {
    records?: ArticleListVO[]
    total?: number
    pageNum?: number
    pageSize?: number
  }

  type ArticleQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    category?: string
    startTime?: string
    endTime?: string
    keyword?: string
    status?: string
    sort?: string
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseArticleDetailVO = {
    code?: number
    data?: ArticleDetailVO
    message?: string
  }

  type BaseResponseArticlePageVO = {
    code?: number
    data?: ArticlePageVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseCrawlTaskLog = {
    code?: number
    data?: CrawlTaskLog
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponsePageCrawlTaskLog = {
    code?: number
    data?: PageCrawlTaskLog
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type ChatHistory = {
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ChatHistoryQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    lastCreateTime?: string
  }

  type chatRagAndAIParams = {
    userQuestion: string
  }

  type chatToGenCodeParams = {
    appId: number
    message: string
  }

  type CrawlLogQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    startDate?: string
    endDate?: string
    taskStatus?: string
  }

  type CrawlTaskLog = {
    id?: number
    taskDate?: string
    startTime?: string
    endTime?: string
    totalCrawl?: number
    successCount?: number
    failCount?: number
    failReason?: string
    taskStatus?: string
    operator?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type downloadAppCodeParams = {
    appId: number
  }

  type executeWorkflowParams = {
    prompt: string
  }

  type getAppVOByIdByAdminParams = {
    id: number
  }

  type getAppVOByIdParams = {
    id: number
  }

  type getArticleDetailParams = {
    articleId: number
  }

  type getInfoParams = {
    id: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type ImageResource = {
    category?: 'CONTENT' | 'LOGO' | 'ILLUSTRATION' | 'ARCHITECTURE'
    description?: string
    qualityResult?: QualityResult
    url?: string
  }

  type listAppChatHistoryParams = {
    appId: number
    pageSize?: number
    lastCreateTime?: string
  }

  type listArticlesParams = {
    queryRequest: ArticleQueryRequest
    current?: number
    pageSize?: number
  }

  type listLogsParams = {
    queryRequest: CrawlLogQueryRequest
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    token?: string
    phone?: string
    email?: string
    userPassword?: string
    createTime?: string
    updateTime?: string
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageCrawlTaskLog = {
    records?: CrawlTaskLog[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type pageParams = {
    page: PageChatHistory
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type QualityResult = {
    isValid?: boolean
    errors?: string[]
    suggestions?: string[]
  }

  type removeParams = {
    id: number
  }

  type ServerSentEventString = true

  type SseEmitter = {
    timeout?: number
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }

  type WorkflowContext = {
    currentStep?: string
    originalPrompt?: string
    imageListStr?: string
    imageList?: ImageResource[]
    enhancedPrompt?: string
    generationType?: 'HTML' | 'MULTI_FILE' | 'VUE_PROJECT'
    generatedCodeDir?: string
    buildResultDir?: string
    errorMessage?: string
  }
}
