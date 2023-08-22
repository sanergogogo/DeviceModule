import { _decorator, assetManager, Button, Camera, Component, EventHandler, ImageAsset, instantiate, Label, Layout, native, Node, RenderTexture, resources, Sprite, SpriteFrame, sys, tween, UITransform, view } from 'cc';
import MultiPlatform, { CheckAppInstalledName, TrackEventData } from './MultiPlatform';
const { ccclass, property } = _decorator;

// 所有功能
const kAllFunctions = {
    'getDeviceName': '设备名字',
    'getDeviceUuid': '设备id',
    'getDeviceAdid': '广告id',
    'getNetworkType': '网络类型',
    'getBatteryLevel': '电池电量',
    'vibrate': '震动效果',
    'copyTextToClipboard': '拷贝文字',
    'setKeepScreenOn': '屏幕常亮',
    'shareText': '分享文字',
    'shareImage': '分享图片',
    'shareVideo': '分享视频',
    'shareFile': '分享文件',
    'checkAppInstalled': 'App是否安装',
    'saveImageToAlbum': '保存图片到相册',
    'selectImageFromAlbum': '选择相册图片',
    'changeOrientation': '横竖屏',
    'doFacebookLogin': 'FB登陆',
    'doAppleLogin': '苹果登陆',
    'trackEventFirebase': 'Firebase事件',
    'trackEventAppsFlyer': 'AppsFlyer事件',
    'trackEventAdjust': 'Adjust事件',
};

@ccclass('App')
export class App extends Component {

    @property(Layout)
    private layout: Layout = null;

    @property(Button)
    private button: Button = null;

    @property(Sprite)
    private sprite: Sprite = null;

    protected onLoad(): void {
        this.layout.node.removeAllChildren();
        this.sprite.node.active = false;

        for (const key in kAllFunctions) {
            this.createButton(kAllFunctions[key], key);
        }
    }

    private createButton(title: string, func: string) {
        let btn = instantiate(this.button.node);
        let com = btn.getComponent(Button);
        btn.getChildByName('Label').getComponent(Label).string = title;

        const clickEventHandler = new EventHandler();
        clickEventHandler.target = this.node; // 这个 node 节点是你的事件处理代码组件所属的节点
        clickEventHandler.component = 'App';// 这个是脚本类名
        clickEventHandler.handler = 'onClickedCallback';
        clickEventHandler.customEventData = func;
        com.clickEvents.push(clickEventHandler);

        this.layout.node.addChild(btn);
    }

    private setSprite(path: string) {
        
        assetManager.loadRemote(path, ImageAsset, (err: any, imageAsset: ImageAsset) => {
            if (err) {
                console.log(err.message);
                return;
            }
            this.sprite.node.active = true;
            this.sprite.spriteFrame = SpriteFrame.createWithImage(imageAsset);
            this.sprite.node.getChildByName('Label').getComponent(Label).string = `width:${this.sprite.spriteFrame.width} height:${this.sprite.spriteFrame.height}`;
            tween(this.sprite.node).delay(5).call(() => {
                this.sprite.node.active = false;
            }).start();
        });
    }

    start() {
        if (sys.isNative)
        {
            // native调用脚本
            native.bridge.onNative = (arg0:string, arg1: string):void=>{
                console.log(arg0, arg1);
                if (arg0 == 'selectImageFromAlbum') {
                    const ret = JSON.parse(arg1);
                    if (ret.filepath) {
                        this.setSprite(ret.filepath);
                    }
                } else if (arg0 == 'FacebookLogin') {
                    const ret = JSON.parse(arg1);
                    if (ret.success) {
                        this.request('/api/account/facebooklogin', {
                            access_token: ret.token
                        }).then((data) => {console.log(data)})
                        .catch((err) => {console.log(err)});
                    }
                }
            }
        }
    }

    private onClickedCallback(event: Event, customEventData: string) {
        if (this[customEventData]) {
            this[customEventData]();
        } else {
            console.log('没有实现函数');
        }
    }

    getDeviceName() {
        console.log(MultiPlatform.instance.getDeviceName());
    }

    getDeviceUuid() {
        console.log(MultiPlatform.instance.getDeviceUuid());
    }

    getDeviceAdid() {
        console.log(MultiPlatform.instance.getDeviceAdid());
    }

    getNetworkType() {
        console.log(MultiPlatform.instance.getNetworkType());
    }

    getNetworkStrength() {
        console.log(MultiPlatform.instance.getNetworkStrength());
    }

    getBatteryLevel() {
        console.log(MultiPlatform.instance.getBatteryLevel());
    }

    vibrate() {
        console.log(MultiPlatform.instance.vibrate('light'));
    }

    copyTextToClipboard() {
        MultiPlatform.instance.copyTextToClipboard('copyTextToClipboard');
    }

    setKeepScreenOn() {
        MultiPlatform.instance.setKeepScreenOn(true);
    }

    shareText() {
        MultiPlatform.instance.shareText('https://google.com');
    }

    shareImage() {
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState === 4 && xhr.status === 200) {
                if (xhr.response && sys.isNative) {
                    const u8a = new Uint8Array(xhr.response);
                    let filePath = native.fileUtils.getWritablePath() + '/share/img.png';
                    native.fileUtils.createDirectory(native.fileUtils.getWritablePath() + '/share/')
                    native.fileUtils.writeDataToFile(u8a, filePath);
                    console.log("Save image data success");
                    MultiPlatform.instance.shareImage(filePath);
                } else {
                    console.log(xhr.response)
                }
            }
        }
        xhr.responseType = "arraybuffer";
        xhr.open("GET", 'https://load.als1.in/yb.png', true);
        xhr.send();
    }

    shareVideo() {
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState === 4 && xhr.status === 200) {
                if (xhr.response && sys.isNative) {
                    const u8a = new Uint8Array(xhr.response);
                    let filePath = native.fileUtils.getWritablePath() + 'video.mp4';
                    native.fileUtils.writeDataToFile(u8a, filePath);
                    console.log("Save image data success");
                    MultiPlatform.instance.shareVideo(filePath);
                } else {
                    console.log(xhr.response)
                }
            }
        }
        xhr.responseType = "arraybuffer";
        xhr.open("GET", 'https://load.als1.in/IMG_0561.MP4', true);
        xhr.send();
    }

    shareFile() {
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState === 4 && xhr.status === 200) {
                if (xhr.response && sys.isNative) {
                    const u8a = new Uint8Array(xhr.response);
                    let filePath = native.fileUtils.getWritablePath() + 'jquery.min.js';
                    native.fileUtils.writeDataToFile(u8a, filePath);
                    console.log("Save image data success");
                    MultiPlatform.instance.shareFile(filePath);
                } else {
                    console.log(xhr.response)
                }
            }
        }
        xhr.responseType = "arraybuffer";
        xhr.open("GET", 'https://load.als1.in/weui/jquery.min.js', true);
        xhr.send();
    }

    checkAppInstalled() {
        console.log(MultiPlatform.instance.checkAppInstalled(CheckAppInstalledName.WEIXIN));
    }

    saveImageToAlbum() {
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState === 4 && xhr.status === 200) {
                if (xhr.response && sys.isNative) {
                    const u8a = new Uint8Array(xhr.response);
                    let filePath = native.fileUtils.getWritablePath() + 'imgsave.png';
                    native.fileUtils.writeDataToFile(u8a, filePath);
                    console.log("Save image data success");
                    MultiPlatform.instance.saveImageToAlbum(filePath);
                } else {
                    console.log(xhr.response)
                }
            }
        }
        xhr.responseType = "arraybuffer";
        xhr.open("GET", 'https://load.als1.in/yb.png', true);
        xhr.send();
    }

    selectImageFromAlbum() {
        MultiPlatform.instance.selectImageFromAlbum({
            type: 0,
            filename: 'img.png',
            aspectRatioX: 1,
            aspectRatioY: 1,
            maxWidth: 500,
            maxHeight: 500,
        });
    }

    changeOrientation() {
        MultiPlatform.instance.changeOrientation(0);
    }

    doFacebookLogin() {
        console.log(MultiPlatform.instance.doFacebookLogin());
    }

    doAppleLogin() {
        console.log(sys.osVersion, sys.osMainVersion);
        console.log(MultiPlatform.instance.doAppleLogin());
    }

    trackEventFirebase() {
        const eventData : TrackEventData<0> = {
            event_name: 'normal_event',
            event_type: 0,
        };
        MultiPlatform.instance.trackEventFirebase(eventData);
        
        const eventData1 : TrackEventData<1> = {
            event_name: 'purchase',
            event_type: 1,
            value: 100.00,
            currency: 'USD',
            transaction_id: '123123'
        };
        MultiPlatform.instance.trackEventFirebase(eventData1);
    }

    trackEventAppsFlyer() {
        const eventData : TrackEventData<0> = {
            event_name: 'normal_event',
            event_type: 0,
        };
        MultiPlatform.instance.trackEventAppsFlyer(eventData);
        
        const eventData1 : TrackEventData<1> = {
            event_name: 'purchase',
            event_type: 1,
            value: 100.00,
            currency: 'USD',
            transaction_id: '123123'
        };
        MultiPlatform.instance.trackEventAppsFlyer(eventData1);
    }

    trackEventAdjust() {
        const eventData : TrackEventData<0> = {
            event_name: 'normal_event',
            event_type: 0,
        };
        MultiPlatform.instance.trackEventAdjust(eventData);
        
        const eventData1 : TrackEventData<1> = {
            event_name: 'purchase',
            event_type: 1,
            value: 100.00,
            currency: 'USD',
            transaction_id: '123123'
        };
        MultiPlatform.instance.trackEventAdjust(eventData1);
    }

    /**
     * 简单封装fetch函数给登陆服务器发送请求
     * @param url api url 比如'/api/account/login'
     * @param body 
     * @returns 
     * @example
     * request('/api/account/login', {account:'test',password:'123456'}).then((data) => {console.log(data)}).catch((err) => {console.log(err)});
     */
    public request(url: string, body: object): Promise<any> {
        console.log('request http:', url, body);
        return fetch(`http://127.0.0.1:8787/${url}`, {
            method: 'POST',
            body: JSON.stringify(body),
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response: Response) => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error(response.statusText);
            }
        });
    }
}

