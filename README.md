## ios项目
#### 由于CocosCreator每次构建ios项目会重置xcode的项目文件，所以需要手动添加
1. Bundle Display Name App显示名字
2. Privacy - Photo Library Usage Description 相册
3. Privacy - Tracking Usage Description 这个是广告标识符的权限，如果有使用需要添加权限以及下面2项
   1. 添加AdSupport.framework
   2. 添加AppTrackingTransparency.framework
4. Queried URL Schemes 如果有用到分享、查询app是否安装、打开外部app等 需要添加Schemes
5. 在项目重新引用相关文件
