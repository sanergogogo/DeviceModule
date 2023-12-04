## android项目
1. 在项目根目录的build.gradle添加
   ```
   allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()   // for com.tbruyelle.rxpermissions2:rxpermissions:0.9.5@aar
        maven { url 'https://jitpack.io' }
    }
   }
   ```
   以及
   ```
   dependencies {
        classpath 'com.android.tools.build:gradle:8.0.2'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files

        classpath 'com.google.gms:google-services:4.3.15' // Google Services plugin firebase
   }
   ```
2. 项目路径下的settings.gradle添加
   ```
   rootProject.name = "DeviceModuleTempl"
   include ':lib_common'
   include ':lib_adjust'
   include ':lib_appsflyer'
   include ':lib_facebook'
   include ':lib_firebase'
   include ':lib_sdkmgr'
   include ':lib_google'
   include ':lib_branch'
   project(':lib_common').projectDir    = new File(NATIVE_DIR, 'lib_common')
   project(':lib_sdkmgr').projectDir    = new File(NATIVE_DIR, 'lib_sdkmgr')
   project(':lib_appsflyer').projectDir    = new File(NATIVE_DIR, 'lib_appsflyer')
   project(':lib_facebook').projectDir    = new File(NATIVE_DIR, 'lib_facebook')
   project(':lib_adjust').projectDir    = new File(NATIVE_DIR, 'lib_adjust')
   project(':lib_firebase').projectDir    = new File(NATIVE_DIR, 'lib_firebase')
   project(':lib_google').projectDir    = new File(NATIVE_DIR, 'lib_google')
   project(':lib_branch').projectDir    = new File(NATIVE_DIR, 'lib_branch')
   ```
## ios项目
#### CocosCreator构建ios项目
1. Bundle Display Name App显示名字
2. Privacy - Photo Library Usage Description 相册
3. Privacy - Tracking Usage Description 这个是广告标识符的权限，如果有使用需要添加权限以及下面2项
   1. 添加AdSupport.framework
   2. 添加AppTrackingTransparency.framework
   3. 添加AuthenticationServices.framework
4. Queried URL Schemes 如果有用到分享、查询app是否安装、打开外部app等 需要添加Schemes
5. 如果有Facebook登陆
   1. 右键点击 Info.plist，然后选择 Open As（打开方式）▸ Source Code（源代码）。
   2. 将下列 XML 代码片段复制并粘贴到文件正文中 (<dict>...</dict>)。
   3. 
      ```
      <key>CFBundleURLTypes</key>
      <array>
      <dict>
      <key>CFBundleURLSchemes</key>
      <array>
         <string>fbAPP-ID</string>
      </array>
      </dict>
      </array>
      <key>FacebookAppID</key>
      <string>APP-ID</string>
      <key>FacebookClientToken</key>
      <string>CLIENT-TOKEN</string>
      <key>FacebookDisplayName</key>
      <string>APP-NAME</string>
      ```
   4. 在 [CFBundleURLSchemes] 键内的 <array><string> 中，将 APP-ID 替换为您的应用编号。
   5. 在 FacebookAppID 键内的 <string> 中，将 APP-ID 替换为您的应用编号。
   6. 在 FacebookClientToken 键内的 <string> 中，将 CLIENT-TOKEN 替换为您在应用面板设置 > 高级 > 客户端口令中找到的值。
   7. 在 FacebookDisplayName 键内的 <string> 中，将 APP-NAME 替换为您的应用名称。
   8. 如要使用任何可将应用切换至 Facebook 应用的 Facebook 对话框（如登录、分享、应用邀请等），应用程序的 Info.plist 还需包含以下代码：
   9. ```
      <key>LSApplicationQueriesSchemes</key>
      <array>
      <string>fbapi</string>
      <string>fb-messenger-share-api</string>
      </array>
      ```
   10. build setting -> Apple LLVM9.1 - Language - Objective C -> Weak References in Manual Retain Release -> YES
6. 在项目重新引用相关文件
## TODO
1. firebase ios未测试
2. apple pay
3. apple login
4. google login待测试
5. ios 消息推送 用firebase
6. 新增branch.io的sdk以实现自定义的深度链接
7. 检测谷歌服务是否存在或者能否使用谷歌支付，这2个好像有点不同，待测试.