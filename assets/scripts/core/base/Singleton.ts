
/**
 * 单例模式
用法：
class Test extends Singleton<Test>() {
    public print() {}
}
Test.instance.print();

 * ts的各种单例泛型实现 也就下面这种用起来比较简单了
 * 但是由于是函数调用返回 还是需要在Singleton<T>后面加括号
 * 所以直接写在类里就可以了 也就几行代码
 *  export default class T {
        private static _instance: T = null;
        public static get instance() : T {
            if (this._instance == null) {
                this._instance = new T();
            }
            return this._instance;
        }
        private constructor() {}
    }
 * 再懒一点的话还可以预定义一个vscode的代码片段:
    "Singleton Class": {
		"prefix": "Singleton",
		"body": [
			"export default class ${1:NewClass} {",
			"    private static _instance: ${1:NewClass} = null;",
			"    public static get instance() : ${1:NewClass} {",
			"        if (this._instance == null) {",
			"            this._instance = new ${1:NewClass}();",
			"        }",
			"        return this._instance;",
			"    }",
			"    private constructor() {}",
			"    $0",
			"}"
		],
		"description": "创建一个单例类"
	}
 */
export function Singleton<T>() {
    class SingletonT {
        protected constructor() {}
        private static _instance: SingletonT = null;
        public static get instance() : T {
            if (this._instance == null) {
                this._instance = new this();
            }
            return this._instance as T;
        }
    }
    return SingletonT;
}
