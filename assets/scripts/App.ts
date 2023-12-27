import { _decorator, assetManager, Button, Camera, Component, EventHandler, ImageAsset, instantiate, Label, Layout, native, Node, RenderTexture, resources, Sprite, SpriteFrame, sys, tween, UITransform, view } from 'cc';
import MultiPlatform, { CheckAppInstalledName, TrackEventData } from './MultiPlatform';
import { Graphics } from 'cc';
import { CQRCode } from './CQRCode';
import { find } from 'cc';
import { size } from 'cc';
import { director } from 'cc';
import { Director } from 'cc';
import { Texture2D } from 'cc';
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
    'qrcode': '二维码',
    'googlepay': 'google支付',
    'captureScreen': '截屏',
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
            transaction_id: '123123',
            product_id: '1'
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
            transaction_id: '123123',
            product_id: '1'
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
            transaction_id: '123123',
            product_id: '1'
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

    qrcode() {
        const qrnode = this.node.getChildByName('QRCode');
        qrnode.active = !qrnode.active;
        const qr = qrnode.getComponent(CQRCode);
        qr.string = 'https://baidu.com';
    }

    googlepay() {
        MultiPlatform.instance.doGooglePay('1', new Date().getTime().toString(), 'inapp');
    }

    /**
     * 截屏函数
     * 在场景新建CaptureCanvas/Camera,属性默认就行
     * @param node 需要截图的节点，如果为null则截整个屏幕
     * @param flipY 是否翻转图片（截图默认是反过来的，翻转会比较慢 可以改用node.scaleY = -1或者spriteFrame.flipUVY=true
     * @returns Promise<SpriteFrame>
     * @example
     * screenshot().then((sf: SpriteFrame) => { // 你的代码 });
     */
    screenshot(node: Node = null, flipY: boolean = true) {
        return new Promise(async (resolve) => {
            const captureCamera = find('CaptureCanvas/Camera').getComponent(Camera);
            const viewSize = view.getVisibleSize();

            const renderTexture = new RenderTexture();

            renderTexture.reset({
                width: viewSize.width,
                height: viewSize.height,
            });
            captureCamera.targetTexture = renderTexture;

            await new Promise((r) => {
                director.once(Director.EVENT_AFTER_DRAW, r);
            });

            let x = 0;
            let y = 0;
            let width = Math.ceil(viewSize.width);
            let height = Math.ceil(viewSize.height);
            if (node) {
                const transform = node.getComponent(UITransform);
                width = Math.ceil(transform.width);
                height = Math.ceil(transform.height);
                const worldPos = node.getWorldPosition();
                x = Math.ceil(worldPos.x);
                y = Math.ceil(worldPos.y);
                
            }

            let buffer = renderTexture.readPixels(x, y, width, height);
            let rtBuffer = buffer;
            if (flipY) {
                rtBuffer = new Uint8Array(width * height * 4);
                for (var i = height - 1; i >= 0; i--) {
                    for (var j = 0; j < width; j++) {
                        rtBuffer[((height - 1 - i) * (width) + j) * 4 + 0] = buffer[(i * width + j) * 4 + 0];
                        rtBuffer[((height - 1 - i) * (width) + j) * 4 + 1] = buffer[(i * width + j) * 4 + 1];
                        rtBuffer[((height - 1 - i) * (width) + j) * 4 + 2] = buffer[(i * width + j) * 4 + 2];
                        rtBuffer[((height - 1 - i) * (width) + j) * 4 + 3] = buffer[(i * width + j) * 4 + 3];
                    }
                }
            }

            const image = new ImageAsset({
                _data: rtBuffer,
                _compressed: false,
                width,
                height,
                format: Texture2D.PixelFormat.RGBA8888,
            });
            const t = new Texture2D();
            t.image = image;
    
            const sf = new SpriteFrame();
            sf.texture = t;
    
            captureCamera.targetTexture = null;
    
            resolve(sf);
        });
    }

    screenshot1({
        x, y, w, h, rt
        }: {
        x?: number,
        y?: number,
        w?: number, // width 需要整数！
        h?: number, // height 需要整数！
        rt?: boolean, // 是否翻转图片（截图默认是反过来的，翻转会比较慢 可以改用node.scaleY = -1
        } = {}) {
        return new Promise(async function (resolve) {
        const captureCamera = find('CaptureCanvas/Camera').getComponent(Camera);

        const viewSize = view.getVisibleSize();

        const s = size(
            Math.ceil(w || viewSize.width),
            Math.ceil(h || viewSize.height),
        );

        const sf = new SpriteFrame();
        const renderTexture = new RenderTexture();

        renderTexture.reset({
            width: viewSize.width,
            height: viewSize.height,
        });
        captureCamera.targetTexture = renderTexture;

        const {width, height} = s;

        await new Promise(function (r) {
            director.once(Director.EVENT_AFTER_DRAW, r);
        });

        var buffer = renderTexture.readPixels(x || 0, y || 0, width, height);

        var rtBuffer = buffer;
        if (rt) {

            rtBuffer = new Uint8Array(width * height * 4);
            for (var i = height - 1; i >= 0; i--) {
                for (var j = 0; j < width; j++) {
                    rtBuffer[((height - 1 - i) * (width) + j) * 4 + 0] = buffer[(i * width + j) * 4 + 0];
                    rtBuffer[((height - 1 - i) * (width) + j) * 4 + 1] = buffer[(i * width + j) * 4 + 1];
                    rtBuffer[((height - 1 - i) * (width) + j) * 4 + 2] = buffer[(i * width + j) * 4 + 2];
                    rtBuffer[((height - 1 - i) * (width) + j) * 4 + 3] = buffer[(i * width + j) * 4 + 3];
                }
            }
        }

        const image = new ImageAsset({
            _data: rtBuffer,
            _compressed: false,
            width,
            height,
            format: Texture2D.PixelFormat.RGBA8888,
        });
        const t = new Texture2D();
        t.image = image;

        sf.texture = t;

        captureCamera.targetTexture = null;

        resolve(sf);
    });
        }

        captureScreen() {

            this.screenshot().then((sf: SpriteFrame) => {
                this.sprite.node.active = true;
                this.sprite.spriteFrame = sf;
            });
        }
}

