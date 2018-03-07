前言

众所周知，Android不仅系统版本众多，机型众多，而且各个应用市场都各有各的政策和审核速度。每次发布一个版本对于开发的同学来说都是一种煎熬。而且很多时候我们也会想到：

修复的 bug 需要等待下个版本发布窗口才能发布？
已经 ready 的需求排队上线，需要等待其他 Feature Team 合入代码？
老版本升级速度慢？频繁上线版本提醒用户升级，影响用户体验？
他山之石，可以攻玉
针对这些问题，Android热更新出现了。



接入步骤（Tinker）

一、新建项目，添加gradle依赖在项目的 build.gradle 中，添加tinker-patch-gradle-plugin的依赖
[java] view plain copy
buildscript {  
    repositories {  
        jcenter()  
    }  
    dependencies {  
        classpath 'com.android.tools.build:gradle:2.1.0'  
        classpath ('com.tencent.tinker:tinker-patch-gradle-plugin:1.7.7')  
  
        // NOTE: Do not place your application dependencies here; they belong  
        // in the individual module build.gradle files  
    }  
}  
  
allprojects {  
    repositories {  
        jcenter()  
    }  
}  
  
task clean(type: Delete) {  
    delete rootProject.buildDir  
}  
二、在app的gradle文件，我们需要添加tinker的库依赖以及apply tinker的gradle插件
[java] view plain copy
dependencies {  
    compile fileTree(dir: 'libs', include: ['*.jar'])  
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {  
        exclude group: 'com.android.support', module: 'support-annotations'  
    })  
    compile 'com.android.support:appcompat-v7:25.2.0'  
    testCompile 'junit:junit:4.12'  
  
    //可选，用于生成application类  
    provided('com.tencent.tinker:tinker-android-anno:1.7.7')  
    //tinker的核心库  
    compile('com.tencent.tinker:tinker-android-lib:1.7.7')  
  
    compile "com.android.support:multidex:1.0.1"  
}  
三、顺便加一下签名的配置，放在项目目录。建议在没有任何配置的时候进行签名。




在构建项目的时候可能会遇到下面这个问题（至少我遇到了）：



四、对tinker 进行配置，整个build.gradle文件如下所示。
[java] view plain copy
apply plugin: 'com.android.application'  
//apply tinker插件  
apply plugin: 'com.tencent.tinker.patch'  
  
android {  
    compileSdkVersion 25  
    buildToolsVersion "25.0.2"  
  
    defaultConfig {  
        applicationId "com.chenzhi.tinkerdemo"  
        minSdkVersion 15  
        targetSdkVersion 25  
        versionCode 1  
        versionName "1.0"  
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"  
    }  
  
    signingConfigs {  
        release {  
            try {  
                storeFile file("tinkerdemo.jks")  
                storePassword "tinker"  
                keyAlias "tinker"  
                keyPassword "tinker"  
            } catch (ex) {  
                throw new InvalidUserDataException(ex.toString())  
            }  
        }  
    }  
  
    buildTypes {  
        release {  
            minifyEnabled true  
            signingConfig signingConfigs.release  
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'  
        }  
        debug {  
            debuggable true  
            minifyEnabled true  
            signingConfig signingConfigs.release  
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'  
        }  
    }  
}  
  
dependencies {  
    compile fileTree(dir: 'libs', include: ['*.jar'])  
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {  
        exclude group: 'com.android.support', module: 'support-annotations'  
    })  
    compile 'com.android.support:appcompat-v7:25.2.0'  
    testCompile 'junit:junit:4.12'  
  
    //可选，用于生成application类  
    provided('com.tencent.tinker:tinker-android-anno:1.7.7')  
    //tinker的核心库  
    compile('com.tencent.tinker:tinker-android-lib:1.7.7')  
  
    compile "com.android.support:multidex:1.0.1"  
}  
  
def bakPath = file("${buildDir}/bakApk/")  
  
ext {  
    tinkerEnabled = true  
    tinkerOldApkPath = "${bakPath}/app-debug-0331-11-34-51.apk"  
    //proguard mapping file to build patch apk  
    tinkerApplyMappingPath = "${bakPath}/"  
    //resource R.txt to build patch apk, must input if there is resource changed  
    tinkerApplyResourcePath = "${bakPath}/app-debug-0331-11-34-51-R.txt"  
}  
  
def getOldApkPath() {  
    return ext.tinkerOldApkPath  
}  
def getApplyMappingPath() {  
    return ext.tinkerApplyMappingPath  
}  
def getApplyResourceMappingPath() {  
    return  ext.tinkerApplyResourcePath  
}  
  
if (ext.tinkerEnabled) {  
    tinkerPatch {  
        oldApk = getOldApkPath()  
        ignoreWarning = false  
        useSign = true  
  
//        packageConfig {  
//  
//            configField("TINKER_ID", "2.0")  
//        }  
        buildConfig{  
            tinkerId = "1.0"  
            applyMapping = getApplyMappingPath()  
            applyResourceMapping = getApplyResourceMappingPath()  
        }  
  
        lib {  
  
            pattern = ["lib/armeabi/*.so"]  
        }  
  
        res {  
  
            pattern = ["res/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]  
  
            ignoreChange = ["assetsmple_meta.txt"]  
  
            largeModSize = 100  
        }  
  
        sevenZip {  
  
            zipArtifact = "com.tencent.mm:SevenZip:1.1.10"  
        }  
  
        dex {  
  
            dexMode = "jar"  
  
            pattern = ["classes*.dex",  
                       "assetscondary-dex-?.jar"]  
  
            loader = ["com.tencent.tinker.loader.*",  
                      "com.tencent.tinker.*",  
                      "com.chenzhi.tinkerdemo.MyTinkerApplication"  
            ]  
        }  
  
  
    }  
}  
  
android.applicationVariants.all { variant ->  
    /**  
     * task type, you want to bak  
     */  
    def taskName = variant.name  
  
    tasks.all {  
        if ("assemble${taskName.capitalize()}".equalsIgnoreCase(it.name)) {  
            it.doLast {  
                copy {  
                    def date = new Date().format("MMdd-HH-mm-ss")  
                    from "${buildDir}/outputs/apk/${project.getName()}-${taskName}.apk"  
                    into bakPath  
                    rename { String fileName ->  
                        fileName.replace("${project.getName()}-${taskName}.apk", "${project.getName()}-${taskName}-${date}.apk")  
                    }  
  
                    from "${buildDir}/outputs/mapping/${taskName}/mapping.txt"  
                    into bakPath  
                    rename { String fileName ->  
                        fileName.replace("mapping.txt", "${project.getName()}-${taskName}-${date}-mapping.txt")  
                    }  
  
                    from "${buildDir}/intermediates/symbols/${taskName}/R.txt"  
                    into bakPath  
                    rename { String fileName ->  
                        fileName.replace("R.txt", "${project.getName()}-${taskName}-${date}-R.txt")  
                    }  
                }  
            }  
        }  
    }  
}  

五、添加你项目的 Application ,使之继承DefaultApplicationLike。（配置application需要仔细）
[java] view plain copy
@SuppressWarnings("unused")  
//这里的application是manifest里面的 不需要实际写出类  
@DefaultLifeCycle(application = "com.chenzhi.tinkerdemo.MyTinkerApplication",  
        flags = ShareConstants.TINKER_ENABLE_ALL,  
        loadVerifyFlag = false)  
//更改为继承DefaultApplicationLike  
public class MyApplication extends DefaultApplicationLike {  
  
    public MyApplication(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {  
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);  
    }  
  
    /** 
     * install multiDex before install tinker 
     * so we don't need to put the tinker lib classes in the main dex 
     * 
     * @param base 
     */  
    //以前application写在oncreate的东西搬到这里来初始化  
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)  
    @Override  
    public void onBaseContextAttached(Context base) {  
        super.onBaseContextAttached(base);  
        //you must install multiDex whatever tinker is installed!  
        MultiDex.install(base);  
  
        //installTinker after load multiDex  
        //or you can put com.tencent.tinker.** to main dex  
        TinkerInstaller.install(this);  
    }  
  
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)  
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {  
        getApplication().registerActivityLifecycleCallbacks(callback);  
    }  
}  
六、千万千万千万别忘记配置清单文件AndroidManifest，这里会报红，Build一下就好了，不好也没关系，但是不能有红色下划线。

这里MyTinkerApplication指向的是MyApplication里面声明的



关于application还有一个地方需要注意：



别忘了加权限，因为这个案例是本地环境，我直接将补丁放到了手机sdcard里面，所以需要添加一个读写权限



[java] view plain copy
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />  
七、至此，tinker环境已经搭建完成了，现在来进行测试吧。

在activity里新建一下代码：

[java] view plain copy
public class MainActivity extends AppCompatActivity {  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
  
        TextView mTvBug = (TextView) findViewById(R.id.tv_bug);  
          
        mTvBug.setText("现在是报错状态了");  
  
        Toast.makeText(this, "现在是报错状态了", Toast.LENGTH_SHORT).show();  
  
        //进行补丁的操作，暂时用本地代替  
        TinkerInstaller.onReceiveUpgradePatch(this,  
                Environment.getExternalStorageDirectory().getAbsolutePath()+"/ysb/patch_signed_7zip.apk");  
    }  
}  
八、先运行一个 应用出错的程序在手机上作为上线出bug的版本，运行图中的assembleDebug ，会在图一中生成debug文件。根据出错的apk的日期，去设置build.gredle 中 出错包的信息。

image1



             image2

九、修改activity中的代码，当做是你修改好的补丁包：

[java] view plain copy
public class MainActivity extends AppCompatActivity {  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
  
        TextView mTvBug = (TextView) findViewById(R.id.tv_bug);  
  
        mTvBug.setText("tinker已经修复好了");  
  
        Toast.makeText(this, "tinker已经修复好了", Toast.LENGTH_SHORT).show();  
  
        //进行补丁的操作，暂时用本地代替  
        TinkerInstaller.onReceiveUpgradePatch(this,  
                Environment.getExternalStorageDirectory().getAbsolutePath()+"/ysb/patch_signed_7zip.apk");  
    }  
}  
十、运行tinkerPatchDebug，开始生成补丁：

最后，我通过USB将修改后的补丁包放到手机sdcard中（因为我的手机没有root，不可以通过命令adb push来操作）。
最终效果，手机录不了GIF，只能是静态图了。





大功告成了！
