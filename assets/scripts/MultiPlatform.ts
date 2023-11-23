import { Singleton } from "./core/base/Singleton";
import { Event, log, macro, math, native, ResolutionPolicy, screen, sys, view, _decorator, __private } from 'cc';

/**
 * 检测app安装需要提供的名字
 * android平台需要在<queries><package android:name="com.facebook.katana" /></queries>显式提供app的包名
 * ios平台需要在info.plist提供LSApplicationQueriesSchemes指定的Schemes
 */
export enum CheckAppInstalledName {
    WEIXIN = 0,
    FACEBOOK,
    WHATSAPP,
    TWITTER,
}
const AndroidAppPackageNames: string[] = [
    'com.tencent.mm',
    'com.facebook.katana',
    'com.whatsapp',
    'com.twitter.android',
];
const IosAppSchemes: string[] = [
    'weixin',
    'facebook',
    'whatsapp',
    'twitter',
];

// 选择相册图片参数
export type SelectImageCropParam = {
    type: number,   // 0不裁剪返回原始图片 1裁剪
    filename: string,   // 保存的文件名 比如1.jpg
    aspectRatioX: number,  // 图片剪切的宽高比
    aspectRatioY: number,   // 图片剪切的宽高比 aspectRatioX:aspectRatioY 1:1 16:9等
    maxWidth: number,       // 图片最大返回宽
    maxHeight: number,       // 图片最大返回高
};

// 埋点追踪事件
export type TrackEventDataBase = {
    event_name: string,         // 事件名
    event_type: number,         // 事件类型 0普通事件 1付费事件
    value: number,             // 付费金额 付费事件必须传
    currency: string,          // 付费金额单位 付费事件必须传
    transaction_id: string,    // 付费订单号 付费事件必须传
    product_id: string,         // 商品id
};

export type TrackEventData<EventType extends number> = 
    EventType extends 1 ? 
    (TrackEventDataBase & { event_type: 1 })
    :(Pick<TrackEventDataBase, 'event_name' | 'event_type'> & { event_type: 0 });


/**
 * 多平台管理
 */
export default class MultiPlatform extends Singleton<MultiPlatform>() {

    private constructor() {
        super();
    }

    /**
     * 获取设备名字
     * ios 返回类似iPhone14,3的字符串，可读名称iPhone 13 Pro Max在脚本做映射，可以方便当新机型发布时增加
     * android 返回android.os.Build.BRAND + "|" + android.os.Build.MODEL
     * @returns 
     */
    public getDeviceName(): string {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "getDeviceName", "()Ljava/lang/String;");
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("DeviceModule", "getDeviceName");
            }
        } else if (sys.isBrowser) {
            return 'browser';
        } else {
            return 'unknown';
        }
    }

    /**
     * 获取当前设备唯一标识
     * @returns
     */
    public getDeviceUuid(): string {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "getDeviceUuid", "()Ljava/lang/String;");
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("DeviceModule", "getDeviceUuid");
            }
        } else if (sys.isBrowser) {
            return 'browser';
        } else {
            return 'unknown';
        }
    }

    /**
     * 获取当前设备广告标识 如果没有获取到广告标识，返回getDeviceUuid
     * @returns
     */
    public getDeviceAdid(): string {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "getDeviceAdid", "()Ljava/lang/String;");
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("DeviceModule", "getDeviceAdid");
            }
        } else if (sys.isBrowser) {
            return 'browser';
        } else {
            return 'unknown';
        }
    }

    /**
     * 获取当前设备的网络类型, 如果网络类型无法获取，默认将返回 `sys.NetworkType.LAN`
     * @returns 返回 NONE,LAN 或 WWAN。
     */
    public getNetworkType(): __private._pal_system_info_enum_type_network_type__NetworkType {
        return sys.getNetworkType();
    }

    /**
     * 获取网络强度
     * @returns 0-4
     */
    public getNetworkStrength(): number {
        return 4;
    }

    /**
     * 获取当前设备的电池电量，如果电量无法获取，默认将返回 1。
     * @returns 0.0 ~ 1.0
     */
    public getBatteryLevel(): number {
        return sys.getBatteryLevel();
    }

    /**
     * 震动效果 android需要提供权限 <uses-permission android:name="android.permission.VIBRATE"/>
     * @param style 反馈类型
     * @returns 
     */
    public vibrate(style: 'light' | 'medium' | 'heavy') {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                let duration = 0.5;
                if (style == 'medium') {
                    duration = 0.6;
                } else if (style == 'heavy') {
                    duration == 0.7;
                }
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "vibrate", "(F)V", duration);
            } else if (sys.os == sys.OS.IOS) {
                let value = 0;
                if (style == 'medium') {
                    value = 1;
                } else if (style == 'heavy') {
                    value == 2;
                }
                //@ts-ignore
                return native.reflection.callStaticMethod("DeviceModule", "vibrate:", value);
            }
        }
        log('只支持原生平台');
    }

    /**
     * 拷贝文字到剪切板
     * @param text 
     */
    public copyTextToClipboard(text: string) {
        if (sys.isNative) {
            native.copyTextToClipboard(text);
        } else {
            log('只支持原生平台');
        }
    }

    /**
     * 设置屏幕常亮
     * @param text 
     */
    public setKeepScreenOn(keepScreenOn: boolean) {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "setKeepScreenOn", "(Z)V", keepScreenOn);
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("DeviceModule", "setKeepScreenOn:", keepScreenOn);
            }
        } else {
            log('只支持原生平台');
        }
    }

    /**
     * 分享文字
     * @param text 
     * @param app  分享到指定app ios不支持指定app
     */
    public shareText(text: string, app?: CheckAppInstalledName) {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                let packageName = '';
                if (app) {
                    packageName = AndroidAppPackageNames[app];
                }
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "shareText", "(Ljava/lang/String;Ljava/lang/String;)V", text, packageName);
            } else if (sys.os == sys.OS.IOS) {
                let scheme = '';
                if (app) {
                    scheme = IosAppSchemes[app];
                }
                return native.reflection.callStaticMethod("DeviceModule", "shareText:andScheme:", text, scheme);
            }
        } else {
            log('只支持原生平台');
        }
    }

    /**
     * 分享图片
     * @param uri 图片的路径
     * @param app  分享到指定app ios不支持指定app
     */
    public shareImage(uri: string, app?: CheckAppInstalledName) {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                let packageName = '';
                if (app) {
                    packageName = AndroidAppPackageNames[app];
                }
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "shareImage", "(Ljava/lang/String;Ljava/lang/String;)V", uri, packageName);
            } else if (sys.os == sys.OS.IOS) {
                let scheme = '';
                if (app) {
                    scheme = IosAppSchemes[app];
                }
                return native.reflection.callStaticMethod("DeviceModule", "shareImage:andScheme:", uri, scheme);
            }
        } else {
            log('只支持原生平台');
        }
    }

    /**
     * 分享视频
     * @param uri 视频的路径
     * @param app  分享到指定app ios不支持指定app
     */
    public shareVideo(uri: string, app?: CheckAppInstalledName) {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                let packageName = '';
                if (app) {
                    packageName = AndroidAppPackageNames[app];
                }
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "shareVideo", "(Ljava/lang/String;Ljava/lang/String;)V", uri, packageName);
            } else if (sys.os == sys.OS.IOS) {
                let scheme = '';
                if (app) {
                    scheme = IosAppSchemes[app];
                }
                return native.reflection.callStaticMethod("DeviceModule", "shareVideo:andScheme:", uri, scheme);
            }
        } else {
            log('只支持原生平台');
        }
    }

    /**
     * 分享文件
     * @param uri 文件的路径
     * @param app  分享到指定app ios不支持指定app
     */
    public shareFile(uri: string, app?: CheckAppInstalledName) {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                let packageName = '';
                if (app) {
                    packageName = AndroidAppPackageNames[app];
                }
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "shareFile", "(Ljava/lang/String;Ljava/lang/String;)V", uri, packageName);
            } else if (sys.os == sys.OS.IOS) {
                let scheme = '';
                if (app) {
                    scheme = IosAppSchemes[app];
                }
                return native.reflection.callStaticMethod("DeviceModule", "shareFile:andScheme:", uri, scheme);
            }
        } else {
            log('只支持原生平台');
        }
    }

    /**
     * 检测App是否安装
     * @param app 应用名
     */
    public checkAppInstalled(app: CheckAppInstalledName) : boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "checkAppInstalled", "(Ljava/lang/String;)Z", AndroidAppPackageNames[app]);
            } else if (sys.os == sys.OS.IOS) {
                return native.reflection.callStaticMethod("DeviceModule", "checkAppInstalled:", IosAppSchemes[app]);
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * 保存图片到相册
     * @param fullPathForFilename 带全路径的文件名带扩展名
     * @returns 
     */
    public saveImageToAlbum(fullPathForFilename: string): boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "saveImageToAlbum", "(Ljava/lang/String;)Z", fullPathForFilename);
            } else if (sys.os == sys.OS.IOS) {
                return native.reflection.callStaticMethod("DeviceModule", "saveImageToAlbum:", fullPathForFilename);
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * @en Save the image to the path indicated.
     * @zh 保存图片到指定路径。
     * @param data : @en the image data, should be raw data array with uint8 @zh 图片数据, 应为原始数据数组，uint8 格式。
     * @param path : @en the path to save @zh 保存路径
     * @param width : @en the width of the image @zh 图片宽度
     * @param height : @en the height of the image @zh 图片高度
     * @param filePath : @en the file path of the image @zh 图片文件路径
     * @example
     * ```ts
            let renderTexture = new RenderTexture();
            let renderWindowInfo = {
            width: this._width,
            height: this._height
            };
            renderTexture.reset(renderWindowInfo);
            cameras.forEach((camera: any) => {
            camera.targetTexture = renderTexture;
            });
            await this.waitForNextFrame();
            cameras.forEach((camera: any) => {
                camera.targetTexture = null;
            });
            let pixelData = renderTexture.readPixels();
            native.saveImageData(pixelData, path, width, height, filePath).then(()=>{
                console.log("Save image data success");
            }).catch(()=>{
                console.log("Fail to save image data");
            });
        */
    public saveImageData(data: Uint8Array, width: number, height: number, filePath: string): Promise<void> {
        if (sys.isNative) {
            return native.saveImageData(data, width, height, filePath);
        } else {
            return new Promise((resolve, reject) => {
                log('只支持原生平台');
                resolve();
            });
        }
    }

    /**
     * 选择相册图片
     * @param param 裁剪参数 ios只支持裁剪或不裁剪 不支持其他参数
     * @returns 
     */
    public selectImageFromAlbum(param: SelectImageCropParam): boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "selectImageFromAlbum", "(Ljava/lang/String;)Z", JSON.stringify(param));
            } else if (sys.os == sys.OS.IOS) {
                return native.reflection.callStaticMethod("DeviceModule", "selectImageFromAlbum:", JSON.stringify(param));
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * 设置横竖屏 0横屏 1竖屏
     */
    public changeOrientation(orientation: number) {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "changeOrientation", "(I)Z", orientation);
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("DeviceModule", "changeOrientation:", orientation);
            }
        }

        const frameSize = screen.windowSize;
        const desolutionSize = view.getDesignResolutionSize();

        if (orientation == 0) {
            view.setOrientation(macro.ORIENTATION_LANDSCAPE);
            if (frameSize.height > frameSize.width) {
                screen.windowSize = math.size(frameSize.height, frameSize.width);
                view.setDesignResolutionSize(desolutionSize.height, desolutionSize.width, ResolutionPolicy.FIXED_HEIGHT);
            }
        } else {
            view.setOrientation(macro.ORIENTATION_PORTRAIT);
            if (frameSize.width > frameSize.height) {
                screen.windowSize = math.size(frameSize.height, frameSize.width);
                view.setDesignResolutionSize(desolutionSize.height, desolutionSize.width, ResolutionPolicy.FIXED_WIDTH);
            }
        }
    }

    /**
     * Facebook登陆
     * @returns 
     */
    public doFacebookLogin(): boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "doFacebookLogin", "()Z");
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("DeviceModule", "doFacebookLogin");
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * Apple登陆 iOS系统版本需要>=13 如果<13隐藏苹果登陆按钮
     * @returns 
     */
    public doAppleLogin(): boolean {
        if (sys.isNative && sys.os == sys.OS.IOS) {
            //@ts-ignore
            return native.reflection.callStaticMethod("DeviceModule", "doAppleLogin");
        } else {
            log('只支持ios系统');
            return false;
        }
    }

    /**
     * Apple支付
     * @returns 
     */
    public doApplePay(): boolean {
        if (sys.isNative && sys.os == sys.OS.IOS) {
            //@ts-ignore
            return native.reflection.callStaticMethod("DeviceModule", "doApplePay");
        } else {
            log('只支持ios系统');
            return false;
        }
    }

    /**
     * Google支付
     * @returns 
     */
    /**
     * 
     * @param productId 商品ID(从谷歌后台获取)
     * @param orderId 内部订单号
     * @param productType 商品类型 'inapp':消耗品 'subs':订阅
     * @returns 
     */
    public doGooglePay(productId: string, orderId: string, productType: 'inapp' | 'subs' = 'inapp'): boolean {
        if (sys.isNative && sys.os == sys.OS.ANDROID) {
            return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "doGooglePay", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z", productId, orderId, productType);
        } else {
            log('只支持android系统');
            return false;
        }
    }

    /**
     * 是否支持firebase
     */
    public hasFirebase() : boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "hasFirebase", "()Z");
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("FirebaseModule", "hasFirebase");
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * 追踪事件
     * @param eventData
     */
    public trackEventFirebase(eventData: TrackEventData<0> | TrackEventData<1>) : boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "trackEventFirebase", "(Ljava/lang/String;)Z", JSON.stringify(eventData));
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("FirebaseModule", "trackEvent:", JSON.stringify(eventData));
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * 请求android的通知权限
     * @returns 
     */
    public requestNotificationPermission() : boolean {
        if (sys.isNative && sys.os == sys.OS.ANDROID) {
            return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "requestNotificationPermission", "()Z");
        } else {
            log('只支持android系统');
            return false;
        }
    }

    /**
     * 请求android的权限，这里主要是用来存储uuid需要的READ_EXTERNAL_STORAGE权限，只是顺便提供了一个接口。
     * @param permissions 需要动态申请的权限数组 比如['android.permission.READ_EXTERNAL_STORAGE', 'android.permission.WRITE_EXTERNAL_STORAGE']
     * @returns 
     */
    public requestPermissions(permissions: string[]) : boolean {
        if (sys.isNative && sys.os == sys.OS.ANDROID) {
            return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "requestPermissions", "(Ljava/lang/String;)Z", JSON.stringify(permissions));
        } else {
            log('只支持android系统');
            return false;
        }
    }

    /**
     * 获取firebase的推送token 在native.bridge.onNative中接收FirebaseToken事件
     */
    public getTokenFirebase() : boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "getTokenFirebase", "()Z");
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("FirebaseModule", "getTokenFirebase");
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * 是否启用firebase的分析功能，因为有可能只用firebase的推送功能
     * @param enabled 
     */
    public setAnalyticsCollectionEnabledFirebase(enabled: boolean) {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "setAnalyticsCollectionEnabledFirebase", "(Z)V", enabled);
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("FirebaseModule", "setAnalyticsCollectionEnabledFirebase:", enabled);
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * 获取firebase的推送消息
     * @returns 返回json字符串
     */
    public getMessageFirebase() : string {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "getMessageFirebase", "()Ljava/lang/String;");
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("FirebaseModule", "getMessageFirebase");
            }
        } else {
            log('只支持原生平台');
            return '{}';
        }
    }

    /**
     * 是否支持adjust
     */
    public hasAdjust() : boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "hasAdjust", "()Z");
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("AdjustModule", "hasAdjust");
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * 追踪事件
     * @param eventData
     */
    public trackEventAdjust(eventData: TrackEventData<0> | TrackEventData<1>) : boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "trackEventAdjust", "(Ljava/lang/String;)Z", JSON.stringify(eventData));
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("AdjustModule", "trackEvent:", JSON.stringify(eventData));
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * 是否支持appsflyer
     */
    public hasAppsFlyer() : boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "hasAppsFlyer", "()Z");
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("AppsFlyerModule", "hasAppsFlyer");
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

    /**
     * 追踪事件
     * @param eventData
     */
    public trackEventAppsFlyer(eventData: TrackEventData<0> | TrackEventData<1>) : boolean {
        if (sys.isNative) {
            if (sys.os == sys.OS.ANDROID) {
                return native.reflection.callStaticMethod("com/cocos/game/DeviceModule", "trackEventAppsFlyer", "(Ljava/lang/String;)Z", JSON.stringify(eventData));
            } else if (sys.os == sys.OS.IOS) {
                //@ts-ignore
                return native.reflection.callStaticMethod("AppsFlyerModule", "trackEvent:", JSON.stringify(eventData));
            }
        } else {
            log('只支持原生平台');
            return false;
        }
    }

}