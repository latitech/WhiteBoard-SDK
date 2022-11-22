# WhiteBoard-SDK

[![maven central](https://maven-badges.herokuapp.com/maven-central/com.latitech.android/whiteboard/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.latitech.android/whiteboard)

* 实时交互白板的Android版SDK

## 依赖配置

* 推荐gradle自动依赖

```gradle

repositories {
  google()
  mavenCentral()
}

dependencies {
    implementation 'com.latitech.android:whiteboard-sdk:+'

    // 可选，如果项目使用了androidx可以添加此项开启sdk的可空/非空参数注解的识别，在kotlin环境非常有用。
    compileOnly 'androidx.annotation:annotation:1.5.0'
}

android {

    defaultConfig{

        // 当前仅支持arm架构
        ndk {
            abiFilters 'armeabi-v7a' , 'arm64-v8a'
        }
    }

    // 开启java8支持
    compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
    }
    
    packagingOptions {
        // (可选)如果项目集成的其它sdk也带有`libc++_shared.so`动态库，
        // 则编译时可能会报文件重复异常
    	pickFirst '**/libc++_shared.so'
	}
}

```

* 关于混淆：通过mavenCentral方式自动化集成的SDK aar文件自带混淆配置。

## 初始化SDK

* SDK需要在`Application`中初始化一次

```kotlin

class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        WhiteBoard.init(this)
    }
}

```

其中[WhiteBoard](#whiteboard)是白板核心接口类。

## 在布局中引入白板控件

在有白板的activity的layout文件中引入[WhiteBoardView](#whiteboardview)控件，此控件的大小会在可用空间内自动强制为[WhiteBoardSize.aspectRatio](#whiteboardsize)的显示宽高比并尽可能以其中一个维度撑满父布局，用户可以自行设定对齐方式。例如以下可以让控件尽可能大并自动居中：

```xml

<androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

    ...

    <com.latitech.whiteboard.WhiteBoardView
                android:id="@+id/white_board"
                android:layout_width="warp_content"
                android:layout_height="warp_content"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>

```

## 三、创建白板控制器实例

[WhiteBoardClient](#whiteboardclient)是白板功能控制器类，白板控制接口，属性和事件均由此类负责，通过[WhiteBoard.createInstance](#createinstance)创建。
例如：

```kotlin
    val client = WhiteBoard.createInstance()
```

通过[WhiteBoard.createInstance](#createinstance)创建的[WhiteBoardClient](#whiteboardclient)是一个单例，多次调用只会返回同一个实例。

## 加入房间

首先访问自己的服务器获取要加入的白板房间的roomId,token,appId等参数（房间的创建和token的生成由服务器对接SDK服务端接口）。

首先构建进房参数[JoinConfig](#joinconfig)，然后执行[joinRoom](#joinroom)来加入房间。

```kotlin

    val config = JoinConfig(
                    appId,
                    roomId,
                    userId, // 用户自己系统中的唯一用户id
                    token
            ).apply {
                roleId = 6 // 由用户设定的值，指示本用户在房间中的角色，
                           // 方便用户实现自己的权限管理，默认为0
                sessionId = "xxxx" // 如果用户的业务系统支持单用户多点登录并进入同一个白板房间，
                                   // 那么这个id就是全局唯一的用户标识，此时[userId]可能有相同值，
                                   // 但是[sessionId]必须唯一。可空，留空时sdk会自动生成一个随机id。
                nickname = "xxx" // 本用户在房间中的昵称，可空。
                avatar = "http://xxxx/ssss.jpg" // 本用户在房间中的头像地址，可空。
            }

    WhiteBoard.joinRoom(config);

```

如果加入房间成功，会收到[onJoinSuccess](#onjoinsuccess)回调，如果加入失败会收到[onJoinFailed](#onjoinfailed)回调。

## 五、切换白板

新版sdk可以支持同一个房间中存在多个可随时切换的白板，同一时间只能有一个工作的白板，且房间中所有成员同步显示相同的白板。

房间创建时默认携带一个标准白板，可以继续创建更多白板，切换白板既可以由服务器控制，也可以由客户端控制，客户端接口为[WhiteBoard.switchBucket](#switchbucket)。

## 关闭并离开房间

房间关闭时，比如离开房间的`Activity`时，必须调用[leaveRoom](#leaveroom)来退出房间并释放资源，
此方法会同时完成离开房间和资源释放，同时可以自动释放[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)
类型的事件监听器（推荐），多次执行是安全的。

通常情况会把它放在`Activity.onDestroy`中执行，比如：

```kotlin

    override fun onDestroy() {
        WhiteBoard.leaveRoom()
        super.onDestroy()
    }

```

## 关于录制

白板录制回放原理为服务器录制房间中所有的动作指令，客户端通过按序执行记录的动作指令序列来完成回放。

由于需要服务器记录整个房间所有人的动作指令，所以白板服务只提供了服务端录制请求接口。

由于记录内容仅仅是指令序列而不是视频格式，所以只能由白板SDK回放。

## 二、在布局中引入白板控件

在有回放的activity的layout文件中引入[WhiteBoardPlaybackView](#whiteboardplaybackview)控件， 此控件的大小会在可用空间内自动强制为`WhiteBoardSize.aspectRatio`的显示宽高比并尽可能以其中一个维度撑满父布局，用户可以自行设定对齐方式。例如以下可以让控件尽可能大并自动居中：

```xml
<androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

    ...

    <com.latitech.whiteboard.WhiteBoardPlaybackView
                android:id="@+id/playback_view"
                android:layout_width="warp_content"
                android:layout_height="warp_content"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

## 三、创建回放控制器实例

[WhiteBoardPlayback](#whiteboardplayback)是回放功能控制器接口，通过[WhiteBoardPlayback.createInstance](#createinstance-playbackcreateinstance)获取实例，用于控制回放的加载，播放停止等。例如：

```kotlin
    val playback = WhiteBoardPlayback.createInstance()
```

## 四、初始化回放

初始化回放记录只需执行[WhiteBoardPlayback.init](#init-playbackinit)即可，需要指定要回放的记录id，可以从自己的服务器请求获取。

```kotlin
    playback.init('rectrdId')
```

初始化成功后会收到[WhiteBoardPlaybackListener.onInitFinished](#oninitfinished)事件，之后可以执行[playback.play](#play)开始回放

## 五、回放控制

回放控制器有播放、暂停、停止、跳转、小范围校准等方法。

```kotlin
    playback.play() // 播放，或者从暂停状态恢复为继续播放
    playback.pause() // 暂停
    playback.stop() // 停止
    playback.seek(5*60*1000) // 跳转到5分钟位置
    playback.calibrate(-2000) // 回放校准，延迟2秒（通常用于配合音轨同步小范围比如5秒内的偏差）
    playback.calibrate(1000) // 回放校准，追帧1秒（通常用于配合音轨同步小范围比如5秒内的偏差）
```

## 六、监听回放状态和进度

回放的各种事件比如状态和进度变化可以通过设置监听器监听，通过[WhiteBoardPlayback.setListener](#setlistener-playbacksetlistener)设置[WhiteBoardPlaybackListener](#whiteboardplaybacklistener)实例即可。`WhiteBoardPlaybackListener`接口有多种事件可以监听，比如回放初始化完成会错误，回放器回放进度等。

```kotlin
    playback.setListener(object : WhiteBoardPlaybackListener {
            override fun onStatusChanged(status: PlaybackStatus) {
                // PlaybackStatus为状态枚举
                if (status == PlaybackStatus.PREPARED){
                    // 准备就绪可以播放
                    playback.play()
                }
            }

            override fun onProgress(position: Int, duration: Int) {
                // 播放进度回调，position为当前播放位置，duration为总时长，单位毫秒
            }
        })
```

## 七、关闭销毁回放

离开回放界面时务必要调用[WhiteBoardPlayback.realese](#release-playbackrelease)销毁回放控制器，通常在`Activity.onDestroy`中调用即可。

执行`realese`后绑定在此实例上的回放监听器实例会自动移除。

如需在同一个页面中切换回放记录，需要先调用`WhiteBoardPlayback.realese`销毁旧实例，然后通过`WhiteBoardPlayback.createInstance`创建新实例并初始化新回放记录。

## 离线功能

白板SDK支持两种离线

* 一种是纯粹的离线模式，此模式中没有房间概念，纯本地白板，数据不保留，全程不联网，可通过`WhiteBoard.enterOffline`方法打开。
* 另一种是正常房间模式中发生断网后进入的临时离线状态，保证断网后可以继续书写等，联网后可恢复，需要通过`WhiteBoard.offlineConfig`方法启用离线支持，默认不启用离线支持。
* 纯离线模式和离线状态下仅支持部分不需要网络的功能，包括：书写、擦除、选择、移动、激光笔、几何图形、翻页和新增页等。
* 不支持的或不能正常工作的功能主要是文件相关，比如插入文件，翻页时可能不显示已插入过的文件等。
* 由于不管是图片还是文档在插入白板和再次显示的时候都需要服务器支持，需要访问网络，所以离线时不支持文件功能。

## 进入纯离线模式白板

* 纯离线模式同样使用[WhiteBoardClient](#whiteboardclient)和[WhiteBoardView](#whiteboardview)控制和显示白板，用法和常规房间一样
* 进入离线模式，调用[WhiteBoard.enterOffline](#enteroffline)进入离线模式
* 退出离线模式，调用[WhiteBoard.exitOffline](#exitoffline)退出离线模式。

## 启用房间离线状态支持

正常情况下（默认不启用离线支持）房间中如果发生断网SDK会尝试自动重连，重连失败后会触发`onDisconnected`事件并进入`RoomStatus.FAILED`状态，此时白板将不再工作，所有功能不可用或不正常。

但是有少数使用场景中需要在平时拥有实时同步白板，网络差时又需要本地白板的基本可用性，此时可以通过启用离线状态支持来满足断网后的基本功能正常使用。

1. 房间离线状态支持通过方法[WhiteBoard.offlineConfig](#offlineconfig)设置。
2. 调用离线配置`WhiteBoard.offlineConfig`必须在加入房间`WhiteBoard.joinRoom`之前否则无效。
3. 可以通过再次调用`WhiteBoard.joinRoom`来恢复到在线状态。
4. `OfflineConfig.supportOffline`赋值为true表示启用离线支持。
5. `OfflineConfig.onlineAutoSync`表示在网络恢复并重连后是否上报离线期间产生的操作数据，比如笔迹、新建的白板页等，默认为false。

## 房间相关方法

* 房间控制、状态获取和添加房间回调事件的接口都是[WhiteBoard](#whiteboard)类的静态方法

| 方法名称                                    | 方法描述                       |
| ------------------------------------------- | ------------------------------ |
| [init](#init)                               | 初始化白板SDK                  |
| [createInstance](#createinstance)           | 创建新的白板实例               |
| [joinRoom](#joinroom)                       | 进入白板房间                   |
| [leaveRoom](#leaveroom)                     | 离开白板房间                   |
| [addListener](#addlistener)                 | 添加一个房间事件监听器         |
| [removeListener](#removelistener)           | 移除一个房间事件监听器         |
| [clearListener](#clearlistener)             | 清空所有房间监听器             |
| [setDefaultInputMode](#setdefaultinputmode) | 设置白板的默认初始输入模式配置 |
| [setRetry](#setretry)                       | 设置房间断线自动重连次数       |
| [setAntiAlias](#setantialias)               | 设置白板画线抗锯齿开关         |
| [offlineConfig](#offlineconfig)             | 房间中断网后的离线状态配置     |
| [switchBucket](#switchbucket)               | 切换白板                       |
| [getRoomStatus](#getroomstatus)             | 获取当前房间状态               |
| [getRoom](#getroom)                         | 获取当前加入的房间信息         |
| [getMe](#getme)                             | 获取当前房间中的个人信息       |
| [getUsers](#getusers)                       | 获取当前房间中的用户列表       |

## 白板相关方法

* 白板控制、状态获取和添加白板回调事件的接口都是[WhiteBoardClient](#whiteboardclient)接口实例的方法
* [WhiteBoardClient](#whiteboardclient)实例通过[WhiteBoard](#whiteboard)类的静态方法[createInstance](#createinstance)获取

### 1、白板关键方法

* 以下是通常情况下使用白板功能时会需要使用的关键接口

| 方法名称                                                  | 方法描述               |
|-------------------------------------------------------| ---------------------- |
| [addListener](#addlistener-boardaddlistener)          | 添加一个白板事件监听器 |
| [removeListener](#removelistener-boardremovelistener) | 移除一个白板事件监听器 |
| [clearListener](#clearlistener-boardclearlistener)    | 清空所有白板监听器     |

### 2、白板通用的控制方法

* 以下是在大部分[白板模式](#boardmode)下都有效的白板控制方法

| 方法名称                                  | 方法描述               |
| ----------------------------------------- | ---------------------- |
| [setInputMode](#setinputmode)             | 改变白板的输入模式     |
| [setBackgroundColor](#setbackgroundcolor) | 设置白板背景色         |
| [scroll](#scroll)                         | 垂直滚动白板显示区     |
| [insertFile](#insertfile)                 | 向当前白板页中插入文件 |
| [deleteFile](#deletefile)                 | 删除文件               |
| [recover](#recover)                       | 撤销一次擦除的笔迹     |
| [screenshots](#screenshots)               | 白板截图               |
| [cleanBoardPage](#cleanboardpage)         | 清空白板页             |
| [sendMessage](#sendmessage)               | 同步自定义消息         |

### 3、白板当前属性获取方法

* 以下是获取当前的白板属性和状态的查询方法

| 方法名称                                  | 方法描述                       |
| ----------------------------------------- | ------------------------------ |
| [getBucketId](#getbucketid)               | 获取当前打开的白板的`bucketId` |
| [getBoardMode](#getboardmode)             | 获取当前打开的白板的模式       |
| [getStatus](#getstatus)                   | 获取白板当前状态               |
| [getPageList](#getpagelist)               | 获取当前白板的全部页信息列表   |
| [getCurrentPage](#getcurrentpage)         | 获取当前白板页信息             |
| [getBackgroundColor](#getbackgroundcolor) | 获取当前显示的白板背景色       |
| [getInputConfig](#getinputconfig)         | 获取当前使用的白板输入模式     |
| [getActiveWidget](#getactivewidget)       | 获取当前被激活操作的widget     |
| [canRecovery](#canrecovery)               | 是否存在可还原的笔迹           |
| [getViewport](#getviewport)               | 获取当前白板的窗口尺寸信息     |

### 4、白板特定模式下的控制方法 {#board-page-validity}

* 以下白板控制接口在特定[白板模式](#boardmode)才能生效

| 方法名称                            | 方法描述             | 有效模式                                     |
| ----------------------------------- | -------------------- | -------------------------------------------- |
| [newBoardPage](#newboardpage)       | 新建白板页           | `BoardMode.NORMAL`                           |
| [insertBoardPage](#insertboardpage) | 插入新白板页         | `BoardMode.NORMAL`                           |
| [jumpBoardPage](#jumpboardpage)     | 跳转到目标白板页     | `BoardMode.NORMAL`                           |
| [preBoardPage](#preboardpage)       | 后退到上一页         | `BoardMode.NORMAL`                           |
| [nextBoardPage](#nextboardpage)     | 前进到下一页         | `BoardMode.NORMAL`                           |
| [deleteBoardPage](#deleteboardpage) | 删除白板页           | `BoardMode.NORMAL`                           |
| [prePage](#prepage)                 | 文件上翻页           | `BoardMode.PPT_PLAY`，`BoardMode.PDF_SCROLL` |
| [nextPage](#nextpage)               | 文件下翻页           | `BoardMode.PPT_PLAY`，`BoardMode.PDF_SCROLL` |
| [jumpPage](#jumppage)               | 文件跳页             | `BoardMode.PPT_PLAY`，`BoardMode.PDF_SCROLL` |
| [preStep](#prestep)                 | 回退到上一步动画位置 | `BoardMode.PPT_PLAY`                         |
| [nextStep](#nextstep)               | 前进到下一步动画位置 | `BoardMode.PPT_PLAY`                         |
| [jumpStep](#jumpstep)               | 跳转到指定动画位置   | `BoardMode.PPT_PLAY`                         |

## 添加事件监听器

* 所有的房间和白板相关事件都在[WhiteBoardListener](#whiteboardlistener)接口中。
* 其中房间相关事件需要把监听器通过[WhiteBoard.addListener](#addlistener)方法挂载，白板相关事件需要通过[WhiteBoardClient.addListener](#boardaddlistener)挂载。
* 可以在任何时期添加监听器，包括进入房间之前。
* 如果添加[WhiteBoardListener](#whiteboardlistener)的直接子类作为监听器，则添加后必须由用户手动调用[WhiteBoard.removeListener](#removelistener)或[WhiteBoardClient.removeListener](#boardremovelistener)来移除。
* [WhiteBoardListener](#whiteboardlistener)存在一个易用的子接口[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)，如果添加此接口的子类监听器，则用户在调用[WhiteBoard.leaveRoom](#leaveroom)离开房间或调用[WhiteBoard.exitOffline](#exitoffline)退出离线模式时系统会自动清理所有的[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)子类监听器，无需用户手动移除。

### 1、监听房间相关事件 {#room-events}

* 房间相关事件需要把监听器通过[WhiteBoard.addListener](#addlistener)方法挂载。
* 挂载到[WhiteBoard](#whiteboard)上的监听器仅能触发房间相关的事件，不会触发白板相关的事件，下表为可触发的房间相关事件：

| 事件名称                                    | 事件描述                   |
| ------------------------------------------- | -------------------------- |
| [onJoinSuccess](#onjoinsuccess)             | 成功加入房间               |
| [onJoinFailed](#onjoinfailed)               | 加入房间失败               |
| [onReconnecting](#onreconnecting)           | 房间正在自动重连           |
| [onReconnected](#onreconnected)             | 自动重连成功               |
| [onDisconnected](#ondisconnected)           | 房间彻底断开连接           |
| [onEnterOffline](#onenteroffline)           | 重连超时自动进入离线模式   |
| [onUserList](#onuserlist)                   | 当前已经在房间中的用户列表 |
| [onUserJoin](#onuserjoin)                   | 有其它用户加入了房间       |
| [onUserLeave](#onuserleave)                 | 有其它用户离开了房间       |
| [onRoomStatusChanged](#onroomstatuschanged) | 房间状态变化               |

### 2、监听白板相关事件 {#board-events}

* 白板相关事件需要把监听器通过[WhiteBoardClient.addListener](#addlistener-boardaddlistener)挂载。
* 挂载到[WhiteBoardClient](#whiteboardclient)上的监听器仅能触发白板相关的事件，不会触发房间相关的事件，下表为可触发的白板相关事件：

| 事件名称                                                | 事件描述                                               |
| ------------------------------------------------------- | ------------------------------------------------------ |
| [onBoardStatusChanged](#onboardstatuschanged)           | 白板房间状态变化                                       |
| [onWhiteBoardOpened](#onwhiteboardopened)               | 白板成功打开时触发                                     |
| [onWhiteBoardOpenFailed](#onwhiteboardopenfailed)       | 白板打开失败                                           |
| [onWhiteBoardClosed](#onwhiteboardclosed)               | 白板关闭                                               |
| [onBoardPageList](#onboardpagelist)                     | 白板页信息列表                                         |
| [onCurrentBoardPageChanged](#oncurrentboardpagechanged) | 白板当前页变化                                         |
| [onBoardPageInfoChanged](#onboardpageinfochanged)       | 某一个白板页信息变化                                   |
| [onBoardSizeChanged](#onboardsizechanged)               | 白板的虚拟大小发生变化                                 |
| [onBoardScroll](#onboardscroll)                         | 白板内发生滚动                                         |
| [onBackgroundColorChanged](#onbackgroundcolorchanged)   | 白板背景色变化                                         |
| [onWidgetActive](#onwidgetactive)                       | 有widget被激活                                         |
| [onWidgetActionEvent](#onwidgetactionevent)             | widget被执行了某些关键动作                             |
| [onRecoveryStateChanged](#onrecoverystatechanged)       | 笔迹回收站状态变化                                     |
| [onPageCleaned](#onpagecleaned)                         | 页面被清空后触发                                       |
| [onFileScrolled](#onfilescrolled)                       | 文件被滚动到顶部或底部时触发，仅在滚动到上下边界时触发 |
| [onFileLoadedSuccessful](#onfileloadedsuccessful)       | 文件加载成功                                           |
| [onFileLoadingFailed](#onfileloadingfailed)             | 文件加载失败                                           |
| [onFileStateChanged](#onfilestatechanged)               | 文件状态改变                                           |
| [onMessage](#onmessage)                                 | 收到远端发送的自定义消息                               |

## 完全离线白板模式

* 白板SDK支持一种在不依赖网络的离线白板。
* 此模式下仅支持基础的白板画线、图形等标注功能，不支持插入文件和图片。
* 此模式不会保留使用数据，每次重新打开都是一个全新的空白板。
* 此模式适合作为本地画板使用，可以满足一些不需要同步的简单使用场景。
* 离线模式的白板控制和事件监听同样使用[WhiteBoardClient](#whiteboardclient)和[WhiteBoardClient.addListener](#addlistener-boardaddlistener)实现，且[WhiteBoardClient](#whiteboardclient)仍然是通过[WhiteBoard.createInstance](#createinstance)获取的。
* 下表为与白板离线模式相关的关键接口。

| 方法名称                      | 方法描述     |
| ----------------------------- | ------------ |
| [enterOffline](#enteroffline) | 进入离线模式 |
| [exitOffline](#exitoffline)   | 退出离线模式 |

## 白板回放控制

* 白板回控制接口集中在[WhiteBoardPlayback](#whiteboardplayback)接口的实例对象上。
* 通过[WhiteBoardPlayback.createInstance](#createinstance-playbackcreateinstance)来创建回放控制器实例。
* 下表为控制器方法：

| 方法名称                                                     | 方法描述                                         |
|----------------------------------------------------------| ------------------------------------------------ |
| [createInstance](#createinstance-playbackcreateinstance) | 创建一个回放控制器                               |
| [setListener](#setlistener-playbacksetlistener)          | 设置白板回放监听器                               |
| [init](#playbackinit)                                    | 初始化回放器                                     |
| [play](#play)                                            | 播放                                             |
| [stop](#stop)                                            | 停止                                             |
| [pause](#pause)                                          | 暂停                                             |
| [seek](#seek)                                            | 跳转                                             |
| [calibrate](#calibrate)                                  | 快速校准                                         |
| [release](#release-playbackrelease)                      | 释放并销毁回放器                                 |
| [position](#position)                                    | 当前播放位置                                     |
| [duration](#duration)                                    | 回放总时长                                       |
| [status](#playbackstatus)                                | 当前播放器的状态                                 |
| [recordId](#recordid)                                    | 当前回放的id                                     |
| [whiteboardsize](#whiteboardsize)                        | 当前回放白板的大小                               |
| [viewport](#playbackviewport)                            | 当前回放白板的窗口尺寸信息，包括白板的大小和偏移 |

## 监听白板回放事件

* 白板回放事件集中在[WhiteBoardPlaybackListener](#whiteboardplaybacklistener)中。
* 通过[WhiteBoardPlayback.setListener](#setlistener-playbacksetlistener)来设置监听器。
* 调用[WhiteBoardPlayback.release](#release-playbackrelease)释放回放器时已设置的监听器会被自动移除。
* 下表是相关的回放事件。

| 事件名称                                                                    | 事件描述               |
|-------------------------------------------------------------------------| ---------------------- |
| [onInitFinished](#oninitfinished)                                       | 回放初始化成功         |
| [onError](#onerror)                                                     | 回放初始化错误         |
| [onStatusChanged](#onstatuschanged)                                     | 回放状态变化           |
| [onProgress](#onprogress)                                               | 回放进度回调           |
| [onBoardSizeChanged](#onboardsizechanged-playbackonboardsizechanged)    | 白板的虚拟大小发生变化 |
| [onBoardScroll](#onboardscroll-playbackonboardscroll)                   | 白板内发生滚动时触发   |
| [onFileLoadingFailed](#onfileloadingfailed-playbackonfileloadingfailed) | 回放中的文件加载失败   |
| [onMessage](#onmessage-playbackonmessage)                               | 触发了录制的自定义消息 |

## 白板的渲染显示控件

* 在房间模式和离线模式中，用于显示白板内容的控件为[WhiteBoardView](#whiteboardview)，同时仅支持一个渲染控件正常显示。
* 在回放模式中，用于显示回放内容的控件为[WhiteBoardPlaybackView](#whiteboardview)，同时仅支持一个渲染控件正常显示。
* 如果需要禁用[WhiteBoardView](#whiteboardview)的手势交互只需调用控件的`setEnabled`并设为false即可，通过此方式可以实现禁用用户白板操作权限的功能。

# WhiteBoard

白板SDK核心用户接口，所有接口线程安全。主要负责初始化SDK、控制房间以及修改全局和默认配置等。

## init

`public static void init(@NonNull Context context)`

`public static void init(@NonNull Context context , boolean debug)`

`public static void init(@NonNull Context context, boolean debug, boolean extra)`

初始化白板SDK

应用启动后只需调用一次，最好在`Application`中调用。

| 参数    | 描述                                                         |
| ------- | ------------------------------------------------------------ |
| context | Android上下文                                                |
| debug   | 是否开启debug模式，如果为true则sdk会输出一些日志             |
| extra   | 是否初始化额外加载项，额外加载项目前主要是指腾讯X5内核的初始化，用于实现新版文件独占模式 |

## initExtra

`public static void initExtra()`

`public static void initExtra(@Nullable WhiteBoardInitExtraCallback callback)`

初始化额外加载项

当初始化SDK时使用了`public static void init(@NonNull Context context, boolean debug, boolean extra)`重载方法并且`extra`传递了false，则在后续加入房间之前的合适时机需要调用此方法初始化额外加载项，全局初始化一次即可。

如果应用不会用到文件独占模式的白板即`BoardMode.PPT_PLAY`，`BoardMode.PDF_SCROLL`则不初始化额外加载项也可以。

其中参数`WhiteBoardInitExtraCallback`为额外项加载结果回调，参数为布尔值表示成功失败，失败时可以再次调用`initExtra`重试。

## createInstance

`public static WhiteBoardClient createInstance()`

创建新的白板实例

当前实现中仅允许创建一个实例，多次调用会返回同一个实例。

返回值为[WhiteBoardClient](#whiteboardclient)的实例对象，用于控制白板。获取的实例无需显式调用释放方法释放对象。

## joinRoom

`public static void joinRoom(@NonNull JoinConfig config)`

进入白板房间

只有此方法执行成功才能连通白板，目前sdk仅支持同时进入一个房间，多次调用是安全的，但仅有第一次调用的参数有效，后续调用会被忽略，如需进入其它房间，需要先执行[leaveRoom](#leaveroom)。

加入成功后本地会收到[onJoinSuccess](#onjoinsuccess)回调，加入失败则会收到[onJoinFailed](#onjoinfailed)回调。同时远端用户会收到[onUserJoin](#onuserjoin)回调。

如果已经加入成功但是发生了掉线，则SDK会尝试自动重连，并收到[onReconnecting](#onreconnecting)回调。 重连次数可以通过[setRetry](#setretry)
修改，重连成功后会收到[onReconnected](#onreconnected)，重连失败会收到[onDisconnected](#ondisconnected)，此时必须用户手动调用此方法重新加入房间。

此方法为进入房间方法，与初始化回放[WhiteBoardPlayback.init](#init-playbackinit)，进入离线模式[enterOffline](#enteroffline)不能共存。

* 房间模式，纯离线模式，回放模式三种模式互斥，同一时刻只能处于其中一种工作模式，尝试同时进入其它模式会被忽略并打印警告。只有先退出当前模式后才能进入新模式。

| 参数                  | 描述           |
| --------------------- | -------------- |
| [config](#joinconfig) | 房间和身份信息 |

## leaveRoom

`public static void leaveRoom()`

离开白板房间

与[joinRoom](#joinroom)成对，关闭房间时必须调用此方法以释放所有房间和白板资源。

该方法会断开白板连接并释放资源，同时会清理所有的[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)类型的监听器。
多次调用是安全的。 离开白板后远端用户会收到[onUserLeave](#onuserleave)回调。

## enterOffline

`public static void exitOffline()`

`public static void enterOffline(float aspectRatio, @ColorInt int backgroundColor)`

`public static void enterOffline(float aspectRatio, int expansionFactor, @ColorInt int backgroundColor)`

进入离线模式

离线模式是完全不联网的状态，所以只能使用基础白板功能，不能使用插入文件功能。

离线模式中不存在房间信息，所有与房间信息相关接口都处于无效状态。

进入离线模式后白板总是处于[BoardStatus.OFFLINE](#boardstatusoffline)状态， 并且不会触发[onWhiteBoardOpened](#onwhiteboardopened)事件。

* 房间模式，纯离线模式，回放模式三种模式互斥，同一时刻只能处于其中一种工作模式，尝试同时进入其它模式会被忽略并打印警告。只有先退出当前模式后才能进入新模式。

| 参数            | 描述                                              |
| --------------- | ------------------------------------------------- |
| aspectRatio     | 白板宽高比                                        |
| expansionFactor | 可垂直扩展倍率，默认为1即不可扩展（白板不能滚动） |
| backgroundColor | 初始白板背景色，默认0xFFF5F5F5                    |

## exitOffline

`public static void exitOffline()`

退出离线模式

与[enterOffline](#enteroffline)成对，关闭离线模式时必须调用此方法以释放所有白板资源。

## addListener

`public static void addListener(@NonNull WhiteBoardListener listener)`

添加一个房间事件监听器

如果添加[WhiteBoardListener](#whiteboardlistener)的直接子类则会永久存续，直到使用[removeListener](#removelistener)移除。
如果添加[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)的子类则可以在[leaveRoom](#leaveroom)时自动移除，无需手动执行[removeListener](#removelistener)。

可以在[joinRoom](#joinroom)之前或之后添加。添加的监听器仅会收到房间相关的事件，具体事件列表查看[监听房间相关事件](#1-room-events)。

| 参数                            | 描述       |
| ------------------------------- | ---------- |
| [listener](#whiteboardlistener) | 事件监听器 |

## removeListener

`public static void removeListener(@NonNull WhiteBoardListener listener)`

移除一个房间事件监听器

可在任何时候调用。

| 参数                            | 描述       |
| ------------------------------- | ---------- |
| [listener](#whiteboardlistener) | 监听器实例 |

## clearListener

`public static void clearListener()`

清空所有房间监听器

会清空所有的监听器，包括[WhiteBoardListener](#whiteboardlistener)和[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)类型的全部监听器。

可在任何时候调用。

## switchBucket

`public static void switchBucket(@NonNull String bucketId)`

`public static void switchBucket(@NonNull String bucketId, @Nullable SwitchBucketCallback callback)`

切换白板

* 新版房间可以支持在一个房间中创建多个白板，这里我们定义用`bucket`表示白板。
* 目前一个房间中同一时刻只能有一个打开的白板。
* 房间中所有成员会同时看到当前打开的白板画面，所有房间成员看到的内容是一致的。
* 当需要切换白板时，就需要调用此方法来命令房间切换白板。
* 同一个房间中只需一个客户端调用此接口就可以同步切换白板，无需各自分别调用。
* 必须在[joinRoom](#joinroom)成功之后调用才有效。

| 参数                              | 描述                                                         |
| --------------------------------- | ------------------------------------------------------------ |
| bucketId                          | 新的白板id，房间中每个白板也就是`bucket`有一个对应的唯一标识符就是`bucketId`在服务器创建房间和白板时生成。 |
| [callback](#switchbucketcallback) | 切换请求执行结果回调                                         |

## offlineConfig

`public static void offlineConfig(@NonNull OfflineConfig config)`

房间中断网后的离线状态配置，[config](#offlineconfig-offlineconfigclass)是离线功能配置参数。

* 当前白板SDK有两种离线相关的支持，一种是纯粹的离线模式，没有加入房间操作，而是直接开启本地离线白板，全程不需要访问网络（此模式是通过[enterOffline](#enteroffline)进入的）；另一种是正常的房间模式下发生断网后进入的临时离线状态（通过本方法开启支持）。
* 此配置并非用于[enterOffline](#enteroffline)模式，而是用于[joinRoom](#joinroom)。
* 配置必须在`joinRoom`之前设置，否则无效。
* 此配置全局有效，如应用总是开启离线支持，则仅需在应用初始化时设置一次。
* 如果激活此配置，则在房间模式中发生断网且自动重连超出指定次数后会进入离线状态，此时[getRoomStatus](#getroomstatus)处于[RoomStatus.OFFLINE](#roomstatusoffline)而不是[RoomStatus.FAILED](#roomstatusfailed)。
* 在离线状态下可以继续使用白板基础功能，但是不能使用文件相关功能，如插入文件或图片，`PPT_PLAY`模式白板翻页异常等。
* 网络通畅后可以再次执行[joinRoom](#joinroom)重新加入房间恢复成在线状态。
* 如果同时启用了离线操作内容同步，则在状态恢复在线后会将离线时的本地操作全部同步到服务器。
* 默认不启用离线支持。

## setDefaultInputMode

`public static void setDefaultInputMode(@NonNull InputConfig config)`

设置白板的默认初始输入模式配置

在[joinRoom](#joinroom)之前调用有效，已经进入房间时调用此方法不会改变当前的输入模式，仅会影响下次进入房间的输入模式。
如需改变当前房间中的输入模式，请调用[setInputMode](#setinputmode)方法。

此方法用于预先设定加入房间后的初始输入配置，反复加入离开房间不会影响此默认设置。 默认设置不会随[setInputMode](#setinputmode)方法改变，即一次设定长期有效。

| 参数                   | 描述         |
| ---------------------- | ------------ |
| [config](#inputconfig) | 输入模式配置 |

## setRetry

`public static void setRetry(int count)`

设置白板断线自动重连次数

默认为10次，设为0表示不自动重连。

| 参数  | 描述     |
| ----- | -------- |
| count | 重连次数 |

## setAntiAlias

`public static void setAntiAlias(boolean enable)`

设置白板画线抗锯齿开关

* 目前仅对无压感笔模式有效，默认不开启。
* 此抗锯齿并非全局抗锯齿，全局抗锯齿默认开启。
* 开启此抗锯齿可能导致意外的绘图错误，比如可滚动的白板可能出现线条糊屏、掉帧等情况，请谨慎使用。
* 在任何时期调用均有效。

| 参数   | 描述                            |
| ------ | ------------------------------- |
| enable | true表示开启抗锯齿，默认为false |

## getRoomStatus

`public static RoomStatus getRoomStatus()`

获取当前房间状态

与监听[onRoomStatusChanged](#onroomstatuschanged)获得的信息一致。

* 返回
  * 房间状态信息[RoomStatus](#roomstatus)，如果未加入房间则返回`RoomStatus.IDLE`。

## getRoom

`@Nullable public static Room getRoom()`

获取当前加入的房间信息

与监听[onJoinSuccess](#onjoinsuccess)获得的信息一致。

- 返回
  - 房间信息[Room](#room)，如果未加入房间则会返回null。

## getMe

`@Nullable public static RoomMember getMe()`

获取当前房间中的个人信息

与监听[onJoinSuccess](#onjoinsuccess)获得的信息一致。

- 返回
  - 自己的成员信息[RoomMember](#roommember)，如果未加入房间则会返回null。

## getUsers

`@NonNull public static List<RoomMember> getUsers()`

获取当前房间中的全部用户列表（包括自己）

此列表与监听[onUserList](#onuserlist)，[onUserJoin](#onuserjoin)，[onUserLeave](#onuserleave)收集获得的列表一致。

- 返回
  - 一个只读的[RoomMember](#roommember)成员信息列表，如果未加入房间则会返回空列表。

# WhiteBoardClient

白板用户接口，所有接口线程安全。对象实例通过[WhiteBoard.createInstance](#createinstance)创建。

* 现阶段全局仅能创建一个实例，所以多次调用创建方法会获得相同的实例引用。
* 获取的实例没有显式的资源释放方法，无需手动管理释放，内部资源会随着[leaveRoom](#leaveroom)的调用自动释放。

## addListener {#boardaddlistener}

`void addListener(@NonNull WhiteBoardListener listener)`

添加一个白板事件监听器

如果添加[WhiteBoardListener](#whiteboardlistener)的直接子类则会永久存续，直到使用[removeListener](#removelistener-boardremovelistener)移除。
如果添加[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)的子类则可以在[leaveRoom](#leaveroom)或[exitOffline](#exitoffline)时自动移除，无需手动执行[removeListener](#removelistener-boardremovelistener)。

可以在[joinRoom](#joinroom)之前或之后添加。添加的监听器仅会收到白板相关的事件，具体事件列表查看[监听白板相关事件](#2-board-events)。

| 参数                            | 描述       |
| ------------------------------- | ---------- |
| [listener](#whiteboardlistener) | 事件监听器 |

## removeListener {#boardremovelistener}

`void removeListener(@NonNull WhiteBoardListener listener)`

移除一个白板事件监听器

可在任何时候调用。

| 参数                            | 描述       |
| ------------------------------- | ---------- |
| [listener](#whiteboardlistener) | 监听器实例 |

## clearListener {#boardclearlistener}

`void clearListener()`

清空所有白板监听器

会清空所有的监听器，包括[WhiteBoardListener](#whiteboardlistener)和[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)类型的全部监听器。

可在任何时候调用。

## screenshots

`void screenshots(@NonNull ScreenshotsCallback listener)`

白板截图

仅在[WhiteBoardView](#whiteboardview)附加到布局中时有效（即必须有可见的白板），回调[ScreenshotsCallback](#screenshotscallback)将在非主线程执行。

| 参数                            | 描述                                                         |
| ------------------------------- | ------------------------------------------------------------ |
| [listener](#whiteboardlistener) | 截图回调，在非主线程回调，截图成功会返回`Bitmap`，失败返回null |

## setInputMode

`void setInputMode(@NonNull InputConfig config)`

改变白板的输入模式

此方法用于已经连通白板房间时改变用户对白板的输入模式，包括例如笔迹的粗细和颜色等。

| 参数                   | 描述         |
| ---------------------- | ------------ |
| [config](#inputconfig) | 输入模式配置 |

## setBackgroundColor

`void setBackgroundColor(@ColorInt int color)`

设置白板背景色

用于在白板房间中改变当前白板页的背景色，未进入房间调用无效。

* 仅能改变当前页面的背景以及此后新增的页面背景，不会影响已经存在的页面背景。
* 如需设定新房间的初始背景色，请在服务器创建房间时设定。

| 参数  | 描述                 |
| ----- | -------------------- |
| color | 颜色值，不支持透明度 |

## scroll

`void scroll(float offsetY)`

垂直滚动白板显示区

* 目前白板是一个纵向可滚动（高大于宽）的矩形，通常白板的可视区[WhiteBoardViewport](#whiteboardviewport)不能呈现完整白板，需要通过滚动来控制可见区。
* 白板内部可以通过用户的双指操作来滚动白板，无需外部干涉，此方法的目的是方便用户实现类似top按钮或滚动条功能。

无论是调用此方法还是用户通过白板手势滚动了白板（包括远程用户滚动白板），都会触发[onBoardScroll](#onboardscroll)回调。

| 参数    | 描述                                                         |
| ------- | ------------------------------------------------------------ |
| offsetY | 白板的垂直偏移量，此值为总量而非增量，当前值在[WhiteBoardViewport](#whiteboardviewport)中描述 |

## newBoardPage

`void newBoardPage()`

新增白板页

在房间中调用可以在当前页列表末尾插入一个新的页面并会自动跳转到这个新页面。

页面创建成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageList](#onboardpagelist)、[onBoardPageInfoChanged](#onboardpageinfochanged)三个回调。

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## insertBoardPage

`void insertBoardPage(@NonNull String pageId)`

插入新白板页

在指定的页面之前插入一个新白板页，同时白板会自动跳转到新插入的页面。

页面创建成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageList](#onboardpagelist)、[onBoardPageInfoChanged](#onboardpageinfochanged)三个回调。

| 参数   | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| pageId | 目标插入位置的页id，此id来自于页数据，可通过[getPageList](#getpagelist)或[getCurrentPage](#getcurrentpage)获取页列表或当前显示页数据，也可通过[onBoardPageList](#onboardpagelist)和[onCurrentBoardPageChanged](#oncurrentboardpagechanged)回调来收集 |

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## jumpBoardPage

`void jumpBoardPage(@NonNull String pageId)`

`void jumpBoardPage(int no)`

跳转到指定白板页

直接跳页的实现方式。

跳转成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageInfoChanged](#onboardpageinfochanged)回调。

| 参数   | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| pageId | 跳转目标的页id，此id来自于页数据，可通过[getPageList](#getpagelist)或[getCurrentPage](#getcurrentpage)获取页列表或当前显示页数据，也可通过[onBoardPageList](#onboardpagelist)和[onCurrentBoardPageChanged](#oncurrentboardpagechanged)回调来收集 |
| no     | 文档页号，从1开始，没有会自动创建                            |

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## preBoardPage

`void preBoardPage()`

返回到上一页

成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageInfoChanged](#onboardpageinfochanged)回调。

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## nextBoardPage

`void nextBoardPage()`

前进到下一页

成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageInfoChanged](#onboardpageinfochanged)回调。

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## deleteBoardPage

`void deleteBoardPage(@NonNull String pageId)`

删除白板页

删除成功后一定会收到[onBoardPageList](#onboardpagelist)回调，如果删除的是当前页会同时触发[onCurrentBoardPageChanged](#oncurrentboardpagechanged)， 如果删除当前页时仅有的一页，则白板会忽略本次操作。

| 参数   | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| pageId | 要删除的页id，此id来自于页数据，可通过[getPageList](#getpagelist)或[getCurrentPage](#getcurrentpage)获取页列表或当前显示页数据，也可通过[onBoardPageList](#onboardpagelist)和[onCurrentBoardPageChanged](#oncurrentboardpagechanged)回调来收集 |

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## cleanBoardPage

`void cleanBoardPage()`

清空白板页

仅在房间加入成功后有效，执行成功后会触发[onPageCleaned](#onpagecleaned)回调。

## prePage

`void prePage()`

文件上翻页

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## nextPage

`void nextPage()`

文件下翻页

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## jumpPage

`void jumpPage(int no)`

文件跳页

| 参数 | 描述                  |
| ---- | --------------------- |
| no   | 目标文件页号，从1开始 |

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## preStep

`void preStep()`

回退到上一步动画位置，当前仅PPT独占模式中有效

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## nextStep

`void nextStep()`

前进到下一步动画位置，当前仅PPT独占模式中有效

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## jumpStep

`void jumpStep(int step)`

跳转到指定动画位置，当前仅PPT独占模式中有效

| 参数 | 描述                  |
| ---- | --------------------- |
| no   | 目标位置序号，从1开始 |

* 方法有效性请查看[白板特定模式下的控制方法](#4-board-page-validity)。

## insertFile

`void insertFile(@NonNull FileConfig config)`

向当前白板页中插入文件

支持的格式有图片jpg,png 文档pdf,doc,docx,dot,wps,wpt,dotx,docm,dotm,rtf
演示文稿ppt,pptx,pot,potx,pps,ppsx,dps,dpt,pptm,potm,ppsm 表格xls,xlsx,xlt,et,ett,xltx,csv,xlsb,xlsm,xltm
如果试图插入不支持的文件类型则会被忽略。

图片尽量上传2K及以下的尺寸，否则某些老旧设备可能无法加载。 office文件需要在线转换格式，所以画面呈现会相对慢一些。

文件插入后会收到大量[onWidgetActionEvent](#onwidgetactionevent)回调（与房间人数有关），此回调仅表达了导致文件状态变化的用户信息和此用户的加载情况，
通常用于在界面上表达房间中各成员对此文件的加载状态。

* 在`BoardMode.PPT_PLAY`，`BoardMode.PDF_SCROLL`模式的白板中仅支持插入图片类型的文件
* 在离线模式或状态下此方法无效

| 参数                  | 描述         |
| --------------------- | ------------ |
| [config](#fileconfig) | 文件配置信息 |

## deleteFile

`void deleteFile(@NonNull String widgetId)`

删除文件

删除后会触发[onWidgetActionEvent](#onwidgetactionevent)
回调，如果删除的文件是当前正在激活的widget，则会收到[onWidgetActive](#onwidgetactive)回调，并且参数为null。

| 参数     | 描述                                                         |
| -------- | ------------------------------------------------------------ |
| widgetId | 文件的widgetId，每个文件都有一个id，可以通过[getActiveWidget](#getactivewidget)方法获取当前用户正在操作的Widget，也可以通过[onWidgetActive](#onwidgetactive)收集当前正在操作的Widget |

## recover

`void recover()`

还原最近一次擦除的笔迹

在输入模式为橡皮模式[InputConfig.erase](#inputconfigerase)时，本用户擦除的笔迹可以通过调用此方法来还原（回滚）。
一次擦除的笔迹指的是用户从落下手指移动擦除线条到抬起手指为止期间擦掉的所有线条。

* 当切换到其它输入模式或者白板翻页后擦除的笔迹缓存将会清空，将无法再还原擦掉的笔迹，即此方法仅在[InputConfig.erase](#inputconfigerase)模式下有效。
* 此方法多次调用是安全的。
* 判断当前是否有可还原的笔迹可以通过调用[canRecovery](#canrecovery)或监听[onRecoveryStateChanged](#onrecoverystatechanged)
  回调获知。

## sendMessage

`void sendMessage(@NonNull String command, String content)`

`void sendMessage(@NonNull String command, @NonNull String content, boolean saveLatest)`

同步自定义消息

消息将会发送给房间中的其它用户，其它用户会在[onMessage](#onmessage)中接收到消息。如果`saveLatest`为true，则再次进入房间或新加入房间的用户会收到该`command`下的最新一条消息。

| 参数       | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| command    | 消息名称                                                     |
| content    | 消息内容                                                     |
| saveLatest | 是否在服务器保留该指令的最新一条消息，在下次进入房间或其它人加入房间时可以收到最新的一条消息，默认为true |

## getBucketId

`@Nullable String getBucketId()`

获取当前打开的白板的bucketId

也可以监听[onWhiteBoardOpened](#onwhiteboardopened)。

* 返回
  * 当前打开的白板bucketId，白板未打开返回null

## getBoardMode

`@NonNull BoardMode getBoardMode()`

获取当前打开的白板的模式

也可以监听[onWhiteBoardOpened](#onwhiteboardopened)。

* 返回
  * 当前白板模式[BoardMode](#boardmode)，白板未打开则返回`BoardMode.NORMAL`

## getStatus

`@NonNull BoardStatus getStatus()`

获取白板当前状态

也可以监听[onBoardStatusChanged](#onboardstatuschanged)。

- 返回
  - 当前的白板状态[BoardStatus](#boardstatus)。

## getPageList

`@NonNull List<WhiteBoardPage> getPageList()`

获取当前白板的全部页信息列表

此列表与监听[onBoardPageList](#onboardpagelist)，[onBoardPageInfoChanged](#onboardpageinfochanged)
处理后获得的列表一致。

* 当前实现在`BoardMode.PDF_SCROLL`模式下此信息无意义，因为此时白板只有很长的一页，文件实际页码信息需要监听[onFileStateChanged](#onfilestatechanged)获取

- 返回
  - 一个只读的[WhiteBoardPage](#whiteboardpage)白板页信息列表，如果未加入房间则会返回空列表。

## getCurrentPage

`@Nullable WhiteBoardPage getCurrentPage()`

获取当前显示的白板页信息

此信息与监听[onCurrentBoardPageChanged](#oncurrentboardpagechanged)获得的信息一致。

* 当前实现在`BoardMode.PDF_SCROLL`模式下此信息无意义，因为此时白板只有很长的一页，文件实际页码信息需要监听[onFileStateChanged](#onfilestatechanged)获取

- 返回
  - 当前白板页信息[WhiteBoardPage](#whiteboardpage)，如果未加入房间则会返回null。

## getBackgroundColor

`@ColorInt int getBackgroundColor()`

获取当前白板页的背景色

此值与监听[onBackgroundColorChanged](#onbackgroundcolorchanged)获得的颜色一致。
可通过调用[setBackgroundColor](#setbackgroundcolor)改变当前白板页的背景。 默认背景色由服务器创建房间时指定。

- 返回
  - 当前白板页颜色值，如果未加入房间则会返回一个固定值`Color.LTGRAY`。

## getInputConfig

`@NonNull InputConfig getInputConfig()`

获取白板当前的输入模式

- 返回
  - 通过[setInputMode](#setinputmode)设置的[InputConfig](#inputconfig)
    ，如果未加入房间则会返回默认配置，默认值可通过[setDefaultInputMode](#setdefaultinputmode)设置。

## getActiveWidget

`@Nullable ActiveWidgetInfo getActiveWidget()`

获取当前被激活操作的widget信息

此信息与监听[onWidgetActive](#onwidgetactive)获得的数据一致。

- 返回
  - [ActiveWidgetInfo](#activewidgetinfo)，如果当前用户没有操作过任何widget或者用户未加入房间则会返回null。

## canRecovery

`boolean canRecovery()`

是否存在可还原的笔迹（擦除还原，仅对笔迹有效）

此值与监听[onRecoveryStateChanged](#onrecoverystatechanged)回调获取的值一致。

- 返回
  - 如果为true表示有可还原的笔迹，此时可通过调用[recover](#recover)来还原一次擦除操作。false时调用[recover](#recover)无效。

## getViewport

`@NonNull WhiteBoardViewport getViewport()`

获取当前白板的可视区信息，包括白板的大小和偏移

此数据与监听[onBoardSizeChanged](#onboardsizechanged)和[onBoardScroll](#onboardscroll)获取的数据一致。
滚动白板可由用户双指手势拖动白板，也可通过程序主动调用[scroll](#scroll)完成。

- 返回
  - 白板的可视区[WhiteBoardViewport](#whiteboardviewport)，通常此值是跟随用户滚动白板而变化，如果未加入房间则会返回固定值[WhiteBoardViewport.IDLE](#whiteboardviewportidle)。

# WhiteBoardListener

房间和白板事件的监听器接口，所有事件响应均在主线程回调。 可通过[Whiteboard.addListener](#addlistener)和[WhiteBoardClient.addListener](#boardaddlistener)添加。

* 其中添加到[Whiteboard](#whiteboard)的监听器只能接收到房间相关的事件，查看[监听房间相关事件](#1-room-events)
* 添加到[WhiteBoardClient](#whiteboardclient)的监听器只能就收到白板相关事件，查看[监听白板相关事件](#2-board-events)

# AutoRemoveWhiteBoardListener

`public interface AutoRemoveWhiteBoardListener extends WhiteBoardListener`

能自动移除的白板事件监听器（推荐使用） 此监听器同样可以在任何时期添加，但是会伴随[leaveRoom](#leaveroom)的调用而自动移除，无需用户手动执行[removeListener](#removelistener)。

## onJoinSuccess

`void onJoinSuccess(@NonNull Room room , @NonNull RoomMember me)`

成功加入白板房间

[joinRoom](#joinroom)成功后的第一个关键事件（[onRoomStatusChanged](#onroomstatuschanged)返回[RoomStatus.CONNECTED](#roomstatusconnected)会先一步触发）。 在这里可以处理一些加入房间成功时的初始化工作。

* 在断线重连成功时同样会触发此事件，之后才会触发[onReconnected](#onreconnected)事件。

| 参数              | 描述                                                         |
| ----------------- | ------------------------------------------------------------ |
| [room](#room)     | 房间信息                                                     |
| [me](#roommember) | 个人信息，由[joinRoom](#joinroom)传递的[JoinConfig](#joinconfig)中携带的信息 |

## onJoinFailed

`void onJoinFailed(int errorCode)`

加入房间失败

[joinRoom](#joinroom)执行失败后触发，因为并没有成功加入房间，所以不会执行自动重连。 失败后需要用户重新调用[joinRoom](#joinroom)来加入房间。
如果已经成功加入了房间但是发生了断线则会触发[onReconnecting](#onreconnecting)尝试自动重连，重连失败则会触发[onDisconnected](#ondisconnected)，而不会触发本事件。

| 参数                              | 描述       |
| --------------------------------- | ---------- |
| [errorCode](#whiteboarderrorcode) | 失败错误码 |

## onReconnecting

`void onReconnecting(int times)`

白板正在自动重连

当白板连接意外断开比如网络波动等，白板会自动尝试重连，在每次尝试开始时会触发此事件。 重连成功后会先触发[onJoinSuccess](#onjoinsuccess)后触发[onReconnected](#onreconnected)， 重连次数达到上限后会触发失败事件[onDisconnected](#ondisconnected)。
首次调用[joinRoom](#joinroom)失败不会自动重连，而是触发[onJoinFailed](#onjoinfailed)。

重连次数默认10次，可通过[setRetry](#setretry)修改。

* 如果启用了[房间离线状态支持](#offlineconfig)，则在重连失败后会进入到离线状态[onEnterOffline](#onenteroffline)而不是连接断开[onDisconnected](#ondisconnected)。

| 参数  | 描述             |
| ----- | ---------------- |
| times | 当前为第几次重试 |

## onReconnected

`void onReconnected()`

自动重连成功

参考[onReconnecting](#onreconnecting)。

## onDisconnected

`void onDisconnected()`

自动重连失败，白板彻底断开连接

即在[onReconnecting](#onreconnecting)重试次数达到上限后触发， 此时需要用户重新执行[joinRoom](#joinroom)加入房间。

## onEnterOffline

`void onEnterOffline()`

重连超时自动进入离线模式

当调用过[WhiteBoard.offlineConfig](#offlineconfig)并且设置`OfflineConfig.supportOffline`为true后。加入房间后如果发生断网并且自动重连超过给定次数后则会自动进入离线状态，并且会触发此事件。

## onWhiteBoardOpened

`void onWhiteBoardOpened(@NonNull String bucketId, @NonNull BoardMode mode)`

白板成功打开时触发

首次加入房间打开默认白板时会触发，[切换白板](#switchbucket)成功后也会触发。

| 参数               | 描述             |
| ------------------ | ---------------- |
| bucketId           | 当前打开的白板id |
| [mode](#boardmode) | 此白板的模式     |

## onWhiteBoardOpenFailed

`void onWhiteBoardOpenFailed(@NonNull String bucketId, int errorCode)`

白板打开失败

首次加入房间打开默认白板失败时会触发，[切换白板](#switchbucket)失败时也会触发。

| 参数                              | 描述             |
| --------------------------------- | ---------------- |
| bucketId                          | 当前失败的白板id |
| [errorCode](#whiteboarderrorcode) | 错误码           |

## onWhiteBoardClosed

`void onWhiteBoardClosed(@NonNull String bucketId)`

白板关闭

[切换白板](#switchbucket)时会先关闭已经打开的白板并触发此事件。

| 参数     | 描述           |
| -------- | -------------- |
| bucketId | 被关闭的白板id |

## onFileLoadedSuccessful

`void onFileLoadedSuccessful()`

文件加载成功

当前仅`BoardMode.PPT_PLAY`和`BoardMode.PDF_SCROLL`模式有效，表示独占文件加载成功。

## onFileLoadingFailed

`void onFileLoadingFailed(int errorCode)`

文件加载失败

当前仅`BoardMode.PPT_PLAY`和`BoardMode.PDF_SCROLL`模式有效，表示独占文件加载失败。

| 参数                              | 描述   |
| --------------------------------- | ------ |
| [errorCode](#whiteboarderrorcode) | 错误码 |

## onFileStateChanged

`void onFileStateChanged(@NonNull Map<String, Object> data)`

文件状态改变

当前仅`BoardMode.PPT_PLAY`和`BoardMode.PDF_SCROLL`模式有效，表示独占文件属性状态改变。

参数`data`是属性的字典集合，在两种模式`BoardMode.PPT_PLAY`和`BoardMode.PDF_SCROLL`有不同的值，如下表：

在`BoardMode.PPT_PLAY`模式中：

|   名称    | 类型 |             描述             |
| :-------: | :--: | :--------------------------: |
|    no     | int  |     当前ppt页号，从1开始     |
|   step    | int  | 当前ppt动画位置索引，从1开始 |
| pageCount | int  |          ppt总页数           |
| stepCount | int  |         ppt总动画数          |

在`BoardMode.PDF_SCROLL`模式中：

|    名称     | 类型 |              描述              |
| :---------: | :--: | :----------------------------: |
| currentPage | int  | 当前显示位置的pdf页号，从1开始 |
|  pageCount  | int  |           pdf总页数            |

## onRoomStatusChanged

`void onRoomStatusChanged(@NonNull RoomStatus status)`

房间状态变化

也可以通过[getRoomStatus](#getroomstatus)主动获取。

从[joinRoom](#joinroom)到[leaveRoom](#leaveroom)之间，只要房间的状态发生变化就会触发此事件。同时此事件触发早于[onJoinSuccess](#onjoinsuccess)，[onJoinFailed](#onjoinfailed)，[onReconnecting](#onreconnecting)等独立事件。 比如调用[joinRoom](#joinroom)后会立即触发[RoomStatus.CONNECTING](#roomstatusconnecting)的变化，[onJoinSuccess](#onjoinsuccess)触发之前会先触发[RoomStatus.CONNECTED](#roomstatusconnected)的变化。

| 参数                  | 描述         |
| --------------------- | ------------ |
| [status](#roomstatus) | 新的房间状态 |

## onBoardStatusChanged

`void onBoardStatusChanged(@NonNull BoardStatus status)`

白板房间状态变化

从[joinRoom](#joinroom)到[leaveRoom](#leaveroom)之间，只要白板的状态发生变化就会触发此事件。同时此事件触发早于[onWhiteBoardOpened](#onwhiteboardopened)，[onWhiteBoardOpenFailed](#onwhiteboardopenfailed)等独立事件。 比如调用[joinRoom](#joinroom)或[switchBucket](#switchbucket)后会立即触发[BoardStatus.LOADING](#boardstatusloading)的变化，[onWhiteBoardOpened](#onwhiteboardopened)触发之前会先触发[BoardStatus.SUCCESSFUL](#boardstatussuccessful)的变化。

| 参数                   | 描述         |
| ---------------------- | ------------ |
| [status](#boardstatus) | 新的白板状态 |

## onUserList

`void onUserList(@NonNull List<RoomMember> users)`

当前已经在房间中的用户列表（包括自己）

加入房间后会触发一次返回已经在房间中的用户，自动重连成功后也会触发。

* 如果自己管理用户列表，需要以此回调的作为列表初始数据，并且在重连成功后重置初始列表。
* 后续的远端用户进出事件由[onUserJoin](#onuserjoin)和[onUserLeave](#onuserleave)反馈。
* [getUsers](#getusers)总是获取当前的完整用户列表。

| 参数  | 描述                                                         |
| ----- | ------------------------------------------------------------ |
| users | 已经在房间中的用户信息[RoomMember](#roommember)列表，此列表为只读列表 |

## onUserJoin

`void onUserJoin(@NonNull RoomMember user)`

有远端用户加入了房间

* 如果自己维护用户列表，注意更新列表数据

| 参数                | 描述     |
| ------------------- | -------- |
| [user](#roommember) | 用户信息 |

## onUserLeave

`void onUserLeave(@NonNull RoomMember user)`

有远端用户离开了房间

* 如果自己维护用户列表，注意更新列表数据

| 参数                | 描述     |
| ------------------- | -------- |
| [user](#roommember) | 用户信息 |

## onBoardPageList

`void onBoardPageList(@NonNull List<WhiteBoardPage> list)`

白板页信息列表

在首次进入房间和白板页列表结构变化时触发，比如新增页，删除页等等。 仅翻页不会触发此事件。

| 参数 | 描述                                                         |
| ---- | ------------------------------------------------------------ |
| list | 白板页信息[WhiteBoardPage](#whiteboardpage)的只读列表，也可以通过[getPageList](#getpagelist)获得 |

## onCurrentBoardPageChanged

`void onCurrentBoardPageChanged(@NonNull WhiteBoardPage page)`

白板当前页改变

在首次加入房间，翻页，切换白板后触发，新增页由于会自动切换到新页面，所以也会触发。

| 参数                    | 描述                                                         |
| ----------------------- | ------------------------------------------------------------ |
| [page](#whiteboardpage) | 当前显示的白板页信息，也可以通过[getCurrentPage](#getcurrentpage)获得 |

## onBoardPageInfoChanged

`void onBoardPageInfoChanged(@NonNull WhiteBoardPage page)`

某一个白板页信息变化

目前仅白板页的缩略图地址发生变化时才会触发此事件，每当白板发生翻页时都会自动更新上一个页面的缩略图， 所以通常情况下此事件触发的白板页信息不是当前正在显示的页面。

* 由于页号[WhiteBoardPage.pageNumber](#whiteboardpage)
  的变化是新增和删除页导致，可能同时影响大量的页信息，所以页号变化没有单独的事件，只能监听[onBoardPageList](#onboardpagelist)观察整个列表的变化。

| 参数                    | 描述                                                         |
| ----------------------- | ------------------------------------------------------------ |
| [page](#whiteboardpage) | 有参数变化的新的页信息，当前仅有缩略图地址变化[WhiteBoardPage.thumbnails](#whiteboardpage) |

## onBoardSizeChanged

`void onBoardSizeChanged(@NonNull WhiteBoardViewport viewport)`

白板大小变化（虚拟大小）

在白板设定的虚拟大小变化时触发，首次进入白板一定会触发一次。

| 参数                            | 描述             |
| ------------------------------- | ---------------- |
| [viewport](#whiteboardviewport) | 白板的可视区数据 |

## onBoardScroll

`void onBoardScroll(@NonNull WhiteBoardViewport viewport)`

白板滚动

白板内发生滚动时触发，主动调用[scroll](#scroll)也会触发，首次进入白板也会触发。

| 参数                            | 描述             |
| ------------------------------- | ---------------- |
| [viewport](#whiteboardviewport) | 白板的可视区数据 |

## onFileScrolled

`void onFileScrolled(@NonNull WidgetScrollInfo info)`

文件被滚动到顶部或底部时触发，仅在滚动到上下边界时触发

此事件仅通过[insertFile](#insertfile)插入的文件会触发，文件独占模式白板（包括`BoardMode.PPT_PLAY`和`BoardMode.PDF_SCROLL`模式）中的独占文件并不会触发此事件。作为替代如果在`BoardMode.PDF_SCROLL`模式时[onBoardScroll](#onboardscroll)会触发并描述当前滚动位置。

| 参数                       | 描述             |
| -------------------------- | ---------------- |
| [info](#widgetscrollinfo ) | 文件滚动状态信息 |

## onBackgroundColorChanged

`void onBackgroundColorChanged(@ColorInt int backgroundColor)`

白板背景色改变

当前的白板背景色变化时触发，首次进入白板也会触发。 可通过[setBackgroundColor](#setbackgroundcolor)随时改变背景色。

| 参数            | 描述       |
| --------------- | ---------- |
| backgroundColor | 新的颜色值 |

## onWidgetActive

`void onWidgetActive(@Nullable ActiveWidgetInfo info)`

有新的widget被激活

用户书写或操作白板时会激活被操作的widget，同时触发此事件，比如移动文件时会收到被移动的文件信息，在白板上写字时会收到白板的信息。

| 参数                      | 描述                                                         |
| ------------------------- | ------------------------------------------------------------ |
| [info](#activewidgetinfo) | 当前激活的widget信息，null表示用户还没有操作，比如刚刚翻页后 |

## onWidgetActionEvent

`void onWidgetActionEvent(@NonNull WidgetActionEvent event)`

widget被执行了某些关键动作

比如有人插入文件或删除文件会收到此事件。 同时每个远端用户的文件加载情况也会触发此事件，通过此事件可以观察到每个人文件加载成功或失败情况。

* 当前仅文件和图片widget会触发此事件。

| 参数                        | 描述           |
| --------------------------- | -------------- |
| [event](#widgetactionevent) | widget事件信息 |

## onRecoveryStateChanged

`void onRecoveryStateChanged(boolean isEmpty)`

笔迹回收站空与非空的状态变化

当在擦除模式[InputConfig.erase](#inputconfigerase)擦除笔迹时被擦除的笔迹会移动到回收站导致回收站不为空，会触发此事件。
当反复调用还原笔迹[recover](#recover)导致回收站为空时会触发此事件。 当从擦除模式切换到其他模式或白板翻页后会自动清空回收站，同样有可能触发此事件。

| 参数    | 描述                                                         |
| ------- | ------------------------------------------------------------ |
| isEmpty | true表示回收站为空，false表示不为空，此时可以通过[recover](#recover)来还原一次擦除操作 |

## onPageCleaned

`void onPageCleaned(@NonNull String pageId)`

页面被清空后触发

当调用[cleanBoardPage](#cleanboardpage)成功后会触发此回调

| 参数   | 描述         |
| ------ | ------------ |
| pageId | 被清空的页id |

## onMessage

`void onMessage(@NonNull String command, @NonNull String content, @NonNull String sessionId)`

收到远端通过[sendMessage](#sendmessage)发送的自定义消息

| 参数      | 描述                    |
| --------- | ----------------------- |
| command   | 消息名称                |
| content   | 消息内容                |
| sessionId | 消息发送者的`sessionId` |

# WhiteBoardPlayback

白板回放控制器，白板可以通过服务器录制房间中发生的所有白板动作，包括切换白板等，由于录制的是动作指令集合而非视频格式，所以回放需要通过此专用回放工具完成。回放时画面渲染依赖[WhiteBoardPlaybackView](#whiteboardplaybackview)控件实现。

## createInstance {#playbackcreateinstance}

`@NonNull static WhiteBoardPlayback createInstance()`

创建一个回放控制器

当前实现中全局仅支持一个回放控制器，多次调用会返回同一个回放控制器

## setListener {#playbacksetlistener}

`void setListener(@Nullable WhiteBoardPlaybackListener listener)`

设置白板回放监听器

在调用[release](#release-playbackrelease)时监听器会被自动释放。

| 参数                                    | 描述                         |
| --------------------------------------- | ---------------------------- |
| [listener](#whiteboardplaybacklistener) | 监听器，传null表示移除监听器 |

## init {#playbackinit}

`void init(@NonNull String recordId)`

初始化回放器

开始回放前必须首先初始化回放器，也就是初始化回放内容。通过传入的回放id，SDK内部会下载对应的回放动作指令文件，初始化就绪后会触发回调[onInitFinished](#oninitfinished)。

* 回放与房间，离线模式不能共存，如果已经加入房间或开启了离线模式，必须先关闭房间或退出离线模式后才能初始化回放。反之如果已经初始化过回放器，必须先释放回放器后才能加入房间或进入离线模式。

| 参数     | 描述                                                   |
| -------- | ------------------------------------------------------ |
| recordId | 回放记录id，服务器录制的内容会生成唯一对应的回访记录id |

## play

`void play()`

播放，如果处于暂停状态则调用播放时会继续播放

## stop

`void stop()`

停止

## pause

`void pause()`

暂停，只有正在播放状态下可以暂停，否则调用会被忽略

## seek

`void seek(int position)`

跳转

* 跳转完成后会自动停留在暂停状态，需要用户手动调用[play](#play)继续播放

| 参数     | 描述               |
| -------- | ------------------ |
| position | 目标位置，单位毫秒 |

## calibrate

`void calibrate(int offset)`

快速校准较小的偏差

通常用于配合其它轨道校准偏差，比如音轨同步。

* 一般用于校准偏差范围很小的偏差值，比如在正负5秒的范围内通常不会被察觉。
* 此方法仅在正在播放状态下有效。
* 方法调用后回放器状态不会改变，仍然处于播放状态。
* 追帧时回放表现为画面前进`offset`时间。
* 等待延迟时回放表现为画面暂停，一直等待`offset`时间后才会继续播放画面，所以用此方法不能实现回退。

| 参数   | 描述                                     |
| ------ | ---------------------------------------- |
| offset | 偏差毫秒，正数表示追帧，负数表示等待延迟 |

## release {#playbackrelease}

`void release()`

释放并销毁回放器

回放不再使用的时候必须调用此方法销毁资源，通常可以放在`Activity.onDestroy`。

* 销毁后此回放器实例不可再使用。
* 如果需要切换回放记录需要先调用此方法释放资源，然后再[创建新回放实例](#createinstance-playbackcreateinstance)并调用初始化[init](#init-playbackinit)。
* 此方法可以多次执行，无需提前调用停止。

## position

`int position()`

获取当前播放位置，单位毫秒

也可以通过监听[onProgress](#onprogress)获得

## duration

`int duration()`

获取回放总时长，单位毫秒

也可以通过监听[onInitFinished](#oninitfinished)或[onProgress](#onprogress)获得

## status {#playbackstatus}

`PlaybackStatus status()`

获取当前播放器的[状态](#playbackstatus)

也可以通过监听[onStatusChanged](#onstatuschanged)获得

## recordId

`String recordId()`

当前回放的id，即[init](#int-playbackinit)中传入的id

## whiteBoardSize {#playbackwhiteboardsize}

`WhiteBoardSize whiteBoardSize()`

获取当前回放[白板的大小](#whiteboardsize)

也可以通过监听[onBoardSizeChanged](#onboardsizechanged-playbackonboardsizechanged)获得

## viewport

`WhiteBoardViewport viewport()`

当前回放[白板的窗口尺寸](#whiteboardviewport)信息，包括白板的大小和偏移

也可以通过监听[onBoardScroll](#onboardscroll-playbackonboardscroll)获得

# WhiteBoardPlaybackListener

白板回放事件监听器，所有事件响应均在主线程。通过[WhiteBoardPlayback.setListener](#setlistener-playbacksetlistener)设置，在[release](#release-playbackrelease)时自动释放。

## onInitFinished

`void onInitFinished(int duration)`

回放初始化成功

执行[WhiteBoardPlayback.init](#init-playbackinit)成功后触发，此时回放器状态对应`PlaybackStatus.PREPARED`。

| 参数     | 描述                 |
| -------- | -------------------- |
| duration | 回放总时长，单位毫秒 |

## onError

`void onError(int errorCode)`

回放初始化错误

执行[WhiteBoardPlayback.init](#init-playbackinit)失败时触发，此时回放器状态对应`PlaybackStatus.ERROR`。

| 参数                              | 描述   |
| --------------------------------- | ------ |
| [errorCode](#whiteboarderrorcode) | 错误码 |

## onStatusChanged

`void onStatusChanged(@NonNull PlaybackStatus status)`

回放状态变化

状态变化事件会早于[onInitFinished](#oninitfinished)和[onError](#onerror)等独立事件。

| 参数                      | 描述     |
| ------------------------- | -------- |
| [status](#playbackstatus) | 回放状态 |

## onProgress

`void onProgress(int position, int duration)`

回放进度回调

在回放处于正在播放状态时，每200毫秒触发一次

| 参数     | 描述                   |
| -------- | ---------------------- |
| position | 当前播放位置，单位毫秒 |
| duration | 回放总时长，单位毫秒   |

## onBoardSizeChanged {#playbackonboardsizechanged}

`void onBoardSizeChanged(@NonNull WhiteBoardSize size)`

回放中白板的虚拟大小发生变化

| 参数                    | 描述             |
| ----------------------- | ---------------- |
| [size](#whiteboardsize) | 白板窗口大小数据 |

## onBoardScroll {#playbackonboardscroll}

`void onBoardScroll(@NonNull WhiteBoardViewport viewport)`

回放中白板内发生滚动

| 参数                            | 描述             |
| ------------------------------- | ---------------- |
| [viewport](#whiteboardviewport) | 新的白板窗口数据 |

## onFileLoadingFailed {#playbackonfileloadingfailed}

`void onFileLoadingFailed(int errorCode)`

回放中文件加载失败

当回放中当前时间点回放的白板模式是`BoardMode.PPT_PLAY`或`BoardMode.PDF_SCROLL`时如果独占文件加载失败则会触发。

| 参数                              | 描述   |
| --------------------------------- | ------ |
| [errorCode](#whiteboarderrorcode) | 错误码 |

## onMessage {#playbackonmessage}

`void onMessage(@NonNull String bucketId, @NonNull String command, @NonNull String content, @NonNull String sessionId)`

收到录制的自定义消息

对应录制时[WhiteBoardClient.sendMessage](#sendmessage)发送的消息。

| 参数      | 描述             |
| --------- | ---------------- |
| bucketId  | 消息所在的白板id |
| command   | 消息名称         |
| content   | 消息内容         |
| sessionId | 消息发送者id     |

# ScreenshotsCallback

截图完成回调

[screenshots](#screenshots)中使用。

## ScreenshotsCallback.done

`void done(@Nullable Bitmap bitmap)`

截图完成

| 参数   | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| bitmap | 截图得到的位图，如果为null表示截图失败，位图大小等于[WhiteBoardView](#whiteboardview)的像素大小 |

# SwitchBucketCallback

切换白板[WhiteBoard.switchBucket](#switchbucket)方法中传递的回调

## SwitchBucketCallback.onSwitchResult

`void onSwitchResult(int code)`

切换白板请求结果

此处状态码返回-1表示成功，但是这个成功并不代表新白板已经加载好了，而是表示切换请求已经被服务器成功受理，白板真正的加载成功仍然要看[onWhiteBoardOpened](#onwhiteboardopened)事件。

| 参数                         | 描述                             |
| ---------------------------- | -------------------------------- |
| [code](#whiteboarderrorcode) | 结果码，-1表示成功，其它表示失败 |

# WhiteBoardInitExtraCallback

初始化额外加载项[WhiteBoard.initExtra](#initextra)完成回调

## WhiteBoardInitExtraCallback.done

`void done(boolean success)`

加载结束

| 参数    | 描述               |
| ------- | ------------------ |
| success | 额外项是否加载成功 |

# WhiteBoardView

房间中白板的显示控件，用于显示白板内容。

此控件的大小会在可用空间内自动强制为[WhiteBoardSize](#whiteboardsize)的显示宽高比并尽可能以其中一个维度撑满父布局，用户可以自行设定对齐方式。

如果需要实现禁用白板书写和操作的功能，只需通过`setEnabled(false)`即可关闭白板的手势操作。

# WhiteBoardPlaybackView

白板回放显示控件，用于在回放中显示白板内容。

此控件的大小会在可用空间内自动强制为[WhiteBoardSize](#whiteboardsize)的显示宽高比并尽可能以其中一个维度撑满父布局，用户可以自行设定对齐方式。

# JoinConfig

加入房间时的参数配置

构造函数
- `public JoinConfig(@NonNull String appId , @NonNull String roomId , @NonNull String userId , @NonNull String token)`

| 参数      | 类型   | 可空 | 描述                                                         |
| --------- | ------ | ---- | ------------------------------------------------------------ |
| appId     | String | 否   | SDK分配的应用id                                              |
| roomId    | String | 否   | 白板的房间id，房间通常由服务器创建                           |
| userId    | String | 否   | 用户业务系统中的稳定用户id                                   |
| token     | String | 否   | 每次加入房间时生成的标识符，与appId，roomId，userId关联，通常由服务器生成 |
| roleId    | int    | 否   | 角色id，默认为0，通常用来标识此用户身份，方便定制用户权限系统 |
| sessionId | String | 是   | 用户会话id，用于唯一标识用户，如果用户业务系统中有与userId对应的临时用户标识符，比如session或token等，此临时id可以在此传递，如果userId相同但是sessionId不同的两个用户加入了白板，可以视为相同用户的多设备加入白板实现，如果留空则白板会自动生成一个 |
| nickname  | String | 是   | 用户名或昵称，在白板中使用的用户名称                         |
| avatar    | String | 是   | 用户头像地址，在白板中显示的用户头像                         |

# OfflineConfig {#offlineconfigclass}

房间中断网后离线状态配置，作为[WhiteBoard.offlineConfig](#offlineconfig)的参数使用。用于开启和设置房间中断网时的离线行为。

如果启用离线支持，则房间中断网重连超时后会自动进入离线状态，基本的白板功能可以继续使用，但不能插入文件和图片等。进入离线后可以再次调用[joinRoom](#joinroom)恢复到在线状态。

## OfflineConfig.supportOffline

`public boolean supportOffline = false`

是否支持离线状态，默认为false

如果设为true，则房间模式中如果发生网络中断并在自动重连超出指定次数后进入到离线状态。

离线状态时房间状态处于`RoomStatus.OFFLINE`。此时可以继续使用白板的基础功能，但是不能插入文件。

* 此状态与[enterOffline](#enteroffline)进入的离线模式不同，此为房间模式断网后的一种临时状态，可以通过[joinRoom](#joinroom)再次恢复成在线状态。

## OfflineConfig.onlineAutoSync

`public boolean onlineAutoSync = false`

当房间从离线状态恢复到在线状态后是否自动同步离线状态时的操作记录到服务器，默认false。

由于离线状态下的操作无法与服务器实时同步，不受服务器正确性校验和有序性处理，同步记录可能导致非预期的白板状态变化，也可能影响正常在线成员的操作记录，所以需要慎重开启此功能。

# InputConfig

输入模式配置 此类仅提供静态工厂方法。

## InputConfig.pen

`public static InputConfig pen(@ColorInt int color , float thickness)`

创建一个笔书写输入模式配置

| 参数      | 描述                                                 |
| --------- | ---------------------------------------------------- |
| color     | 笔颜色，支持透明度，适当的透明度可以看作是马克笔实现 |
| thickness | 笔粗细，必须大于0                                    |

## InputConfig.laserPen

`public static InputConfig laserPen(@NonNull LaserType laserType)`

创建一个激光笔输入模式配置

激光笔是一种瞬时的位置指示型输入模式，指示手指位置的内容。

| 参数                    | 描述       |
| ----------------------- | ---------- |
| [laserType](#lasertype) | 激光笔类型 |

## InputConfig.erase

`public static InputConfig erase(float size)`

创建一个橡皮（擦除）输入模式配置

| 参数 | 描述     |
| ---- | -------- |
| size | 橡皮面积 |

## InputConfig.geometry

`public static InputConfig geometry(@NonNull GeometryType geometryType , @ColorInt int color , float thickness)`

创建一个几何图形输入模式配置

| 参数                          | 描述           |
| ----------------------------- | -------------- |
| [geometryType](#geometrytype) | 图形类型       |
| color                         | 图形边框的颜色 |
| thickness                     | 图形边框粗细   |

## InputConfig.select

`public static InputConfig select()`

创建一个选择输入模式配置

此模式可以在白板中框选内容。

## InputConfig.operation

`public static InputConfig operation()`

创建一个操作模式配置

此模式下白板不能书写，仅能操作白板内元素或滚动缩放白板。

目前仅`BoardMode.PDF_SCROLL`模式下需要通过此方式切换白板书写和pdf滚动操作，其他模式下使用会被忽略并出现错误状态。

# FileConfig

`private FileConfig(Builder builder)`

向白板插入文件时描述文件信息的配置

| 参数      | 类型   | 可空 | 描述                                                         |
| --------- | ------ | ---- | ------------------------------------------------------------ |
| file      | File   | 否   | 要插入的文件，此文件必须有支持的类型后缀，否则会被系统忽略，支持的类型参考[insertFile](#insertfile) |
| name      | String | 是   | 指定文件的实际名称，留空会使用file的名称，此名称不会影响系统对file类型的校验，仅做标识用途，比如file本身是随机串文件名，此处可以赋予它有意义的文件名，此名称会在[ActiveWidgetInfo](#activewidgetinfo)中拿到 |
| left      | float  | 否   | 插入文件时的初始位置的左上角横坐标，默认为0                  |
| top       | float  | 否   | 插入文件时的初始位置的左上角纵坐标，默认为0                  |
| boxWidth  | int    | 否   | 文件外框的宽度，0表示使用文件自身的宽度                      |
| boxHeight | int    | 否   | 文件外框的高度，0表示使用文件自身的高度                      |

# FileConfig.Builder

`public Builder(@NonNull File file)`

文件配置信息构造器，用于创建[FileConfig](#fileconfig)

`file`为要插入的文件，此文件必须有支持的类型后缀，否则会被系统忽略，支持的类型参考[insertFile](#insertfile)

## FileConfig.Builder.fileName

`public Builder fileName(@Nullable String name)`

指定文件的实际名称，留空使用`file`的名称， 此名称不会影响系统对file类型的校验，仅做标识用途，比如file本身是随机串文件名，
此处可以赋予它有意义的文件名，此名称会在[ActiveWidgetInfo](#activewidgetinfo)中拿到

## FileConfig.Builder.location

`public Builder location(float x , float y)`

设定文件插入白板时的初始位置，即文件的左上角坐标，基于白板虚拟坐标系

## FileConfig.Builder.boxSize

`public Builder boxSize(int width , int height)`

设定文件外框的大小，基于白板虚拟坐标系

`width`为文件外框的宽度，0表示使用文件自身的宽度，不允许传负数
`height`为文件外框的高度，0表示使用文件自身的高度，不允许传负数

## FileConfig.Builder.build

`public FileConfig build()`

根据`Builder`中的参数生成[FileConfig](#fileconfig)对象

# ActiveWidgetInfo

被激活的widget信息

白板中的一切都是widget，包括白板，文件，图片，选择框等等，具体参考[WidgetType](#widgettype)。
当用户操作了一个widget或者在它上面书写时，这个widget会被激活，会触发[onWidgetActive](#onwidgetactive)事件。

| 参数              | 类型                      | 可空 | 描述                                                         |
| ----------------- | ------------------------- | ---- | ------------------------------------------------------------ |
| id                | String                    | 否   | widgetId，此widget的唯一标识符，后续对widget的操作都会用到此id，比如[jumpFilePage](#jumpfilepage)和[deleteFile](#deletefile) |
| type              | [WidgetType](#widgettype) | 否   | 指示了此widget的类型                                         |
| userId            | String                    | 是   | 此widget创建者的userId，通常白板页是没有创建者的，由服务器创建 |
| name              | String                    | 是   | widget名称，如果此widget是文件或图片时                       |
| resourceId        | String                    | 是   | 资源id，sdk内部用于标识实际文件的索引，用户通常无需关心      |
| path              | String                    | 是   | 文件路径，如果widget是文件或图片，此为它的本地路径（如果插入的原文件是office文件，则此路径是它转换后的pdf路径，并非原始文件） |
| currentPageNumber | int                       | 否   | 如果widget是文件时，此为当前文件的页码，从1开始              |
| pageCount         | int                       | 否   | 如果widget是文件时，此为文件的总页数（如果原文件是office文件，则此页数是转换成pdf后的实际页数） |

# WidgetScrollInfo

白板中widget滚动事件，通过[insertFile](#insertfile)插入的文件类型widget可以上下滚动，当滚动到顶部或底部时会收到[onFileScrolled](#onfilescrolled)事件，描述了当前文件是否滚动到顶部或底部。

| 参数     | 类型    | 可空 | 描述                         |
| -------- | ------- | ---- | ---------------------------- |
| widgetId | String  | 否   | 当前事件的发生对象的widgetId |
| atTop    | boolean | 否   | 是否在顶部                   |
| atBottom | boolean | 否   | 是否在底部                   |

# Room

房间信息

加入白板房间成功后会收到此数据。

| 参数        | 类型   | 可空 | 描述                                             |
| ----------- | ------ | ---- | ------------------------------------------------ |
| roomId      | String | 否   | 房间的id，与[joinRoom](#joinroom)时的roomId一致  |
| fileGroupId | String | 否   | 白板中的文件在服务器存储的文件组id，用户无需关心 |
| chatRoomId  | int    | 否   | 房间中的聊天室id，暂不支持                       |

# RoomMember

房间中的成员信息

| 参数      | 类型   | 可空 | 描述                                                         |
| --------- | ------ | ---- | ------------------------------------------------------------ |
| userId    | String | 否   | 用户业务系统中的稳定用户id                                   |
| sessionId | String | 否   | 用户会话id，用于唯一标识用户，如果成员在[joinRoom](#joinroom)时未传递此参数，则此参数会由白板自动生成 |
| roleId    | int    | 否   | 角色id，默认为0，通常用来标识此用户身份，方便定制用户权限系统 |
| nickname  | String | 是   | 用户名或昵称                                                 |
| avatar    | String | 是   | 用户头像地址                                                 |

# WhiteBoardPage

白板页信息

| 参数       | 类型   | 可空 | 描述                                                         |
| ---------- | ------ | ---- | ------------------------------------------------------------ |
| pageId     | String | 否   | 白板页id，每个页面的唯一标识符，后续对白板页的操作会用到，比如[jumpBoardPage](#jumpboardpage)，[deleteBoardPage](#deleteboardpage)等 |
| pageNumber | int    | 否   | 页面序号，从1开始，标识了此页是白板中的第几页                |
| thumbnails | String | 否   | 白板页缩略图url，没有时为空字符串                            |

# WhiteBoardSize

白板尺寸信息

此信息的数值基于白板内部的虚拟大小和坐标系，并非实际渲染窗口的纹理大小（实际的纹理大小由[WhiteBoardView](#whiteboardview)的像素大小决定）。
以下参数由服务器创建白板房间时指定，通常在一个房间中此信息是固定不变的。

| 参数          | 类型  | 描述                                                         |
| ------------- | ----- | ------------------------------------------------------------ |
| maxWidth      | int   | 白板最大宽度                                                 |
| maxHeight     | int   | 白板最大高度                                                 |
| displayWidth  | int   | 白板显示宽度（可视区的宽度，当前仅支持与maxWidth保持一致，即只能垂直延展） |
| displayHeight | int   | 白板显示高度（可视区的高度，当此参数小于maxHeight时白板可上下滚动） |
| aspectRatio   | float | 当前白板的显示宽高比                                         |

## WhiteBoardSize.ZERO

`public static final WhiteBoardSize ZERO = new WhiteBoardSize(0 , 0 , 0 , 0);`

一个空尺寸，在未加入白板时获取到的值。

# WhiteBoardViewport

白板当前可视区信息

所有数值基于白板内部的虚拟大小和坐标系，并非实际渲染窗口的纹理大小。 当白板页滚动时会刷新此数据。

| 参数    | 类型                              | 描述                                                  |
| ------- | --------------------------------- | ----------------------------------------------------- |
| size    | [WhiteBoardSize](#whiteboardsize) | 白板尺寸                                              |
| offsetX | float                             | 当前白板水平偏移（当前仅支持垂直滚动，所以此值总是0） |
| offsetY | float                             | 当前白板垂直偏移                                      |

## WhiteBoardViewport.IDLE

`public static final WhiteBoardViewport IDLE = new WhiteBoardViewport(WhiteBoardSize.ZERO , 0 , 0);`

一个空闲值，在未加入白板时获取到的值。

# WidgetActionEvent

widget动作事件

描述了对文件或图片的关键操作信息，包括加载情况，由谁插入或删除等。 由[onWidgetActionEvent](#onwidgetactionevent)提供。

| 参数      | 类型                          | 可空 | 描述                  |
| --------- | ----------------------------- | ---- | --------------------- |
| sessionId | String                        | 否   | 动作发出者的sessionId |
| type      | [WidgetType](#widgettype)     | 否   | widget类型            |
| action    | [WidgetAction](#widgetaction) | 否   | 动作类型              |
| name      | String                        | 是   | widget名称            |

# RoomStatus

房间状态枚举

## RoomStatus.IDLE

空闲状态，表示没有加入房间

## RoomStatus.CONNECTING

正在连接房间，即调用[joinRoom](#joinroom)之后到成功或失败之前的状态。

## RoomStatus.CONNECTED

已加入房间

## RoomStatus.RECONNECTING

正在重连

## RoomStatus.FAILED

连接失败

## RoomStatus.OFFLINE

离线状态

# BoardStatus

白板状态枚举

## BoardStatus.IDLE

空闲状态，表示没有打开白板

## BoardStatus.LOADING

正在打开白板，即调用[joinRoom](#joinroom)或[switchBucket](#switchbucket)之后到成功或失败之前的状态。

## BoardStatus.SUCCESSFUL

打开白板成功

## BoardStatus.FAILED

打开白板失败

## BoardStatus.RECONNECTING

白板正在重连

## BoardStatus.OFFLINE

离线模式

# BoardMode

白板模式枚举，表示了不同的白板类型

## BoardMode.NORMAL

标准白板模式

## BoardMode.PPT_PLAY

ppt演示模式，下层ppt文件独占，上层覆盖透明白板可书写，ppt支持基本的动画

## BoardMode.PDF_SCROLL

pdf卷轴模式，下层pdf文件独占，上层覆盖透明白板可书写，pdf为卷轴式查看

# PlaybackStatus

白板回放状态枚举

## PlaybackStatus.IDLE

初始状态

## PlaybackStatus.LOADING

正在初始化数据，调用[WhiteBoardPlayback.init](#init-playbackinit)之后到加载结束之前的状态

## PlaybackStatus.PREPARED

准备就绪可以播放，调用[WhiteBoardPlayback.init](#init-playbackinit)成功

## PlaybackStatus.PLAYING

正在播放

## PlaybackStatus.PAUSED

暂停

## PlaybackStatus.STOPPED

停止

## PlaybackStatus.ERROR

错误，调用[WhiteBoardPlayback.init](#init-playbackinit)失败

## PlaybackStatus.DESTROYED

对象已销毁，调用[WhiteBoardPlayback.release](#release-playbackrelease)后

# InputMode

输入模式枚举，[InputConfig](#inputconfig)不同构造函数生成的对应模式枚举。

## InputMode.PEN

笔输入模式

## InputMode.ERASE

橡皮输入模式

## InputMode.SELECT

选择输入模式

## InputMode.GEOMETRY

几何图形

## InputMode.OPERATION

操作模式，此模式下白板不能书写，仅能操作白板内元素或滚动缩放白板，前仅`BoardMode.PDF_SCROLL`模式下有效

# GeometryType

几何图形类型

在[InputConfig.geometry](#inputconfiggeometry)中指定要绘制的几何图形。

| 名称      | 图形 |
| --------- | ---- |
| RECTANGLE | 矩形 |
| CIRCLE    | 圆形 |
| LINE      | 直线 |
| ARROW     | 箭头 |

# LaserType

激光笔类型

在[InputConfig.laserPen](#inputconfiglaserpen)中指定激光指示点的样式。

| 名称               | 样式     |
| ------------------ | -------- |
| LASER_DOT          | 圆点     |
| LASER_HAND         | 手形图标 |
| LASER_ARROWS_WHITE | 白色箭头 |
| LASER_ARROWS_BLACK | 黑色箭头 |

# WidgetType

widget类型，白板中的一切都是widget

| 名称      | 类型                                                         |
| --------- | ------------------------------------------------------------ |
| BOARD     | 白板                                                         |
| FILE      | 文件，包括pdf和office                                        |
| IMAGE     | 图片，jpg和png                                               |
| GEOMETRY  | 几何图形，由[InputConfig.geometry](#inputconfiggeometry)模式绘制 |
| SELECTION | 选择框，由[InputConfig.select](#inputconfigselect)模式选中的内容 |

# WidgetAction

widget动作类型

在[onWidgetActionEvent](#onwidgetactionevent)中指示widget具体发生的动作事件。

| 名称       | 事件                  |
| ---------- | --------------------- |
| UPLOAD     | 开始上传/插入新widget |
| DELETE     | 删除widget            |
| SUCCESSFUL | widget加载成功        |
| FAILED     | widget加载失败        |

# WhiteBoardErrorCode

错误码

| 名称                              | 值   | 错误含义                                               |
| --------------------------------- | ---- | ------------------------------------------------------ |
| NONE                              | -1   | 在个别响应事件中表示没有错误，即响应成功               |
| NETWORK_ERROR                     | 100  | 网络不可用                                             |
| SERVER_ERROR                      | 101  | 服务器错误或繁忙                                       |
| ROOM_NOT_CONNECTED                | 150  | 房间未连接                                             |
| APP_ID_NOT_EXIST                  | 200  | appId不存在                                            |
| ROOM_ID_NOT_EXIST                 | 201  | roomId不存在                                           |
| USER_ID_EMPTY                     | 202  | userId为空                                             |
| TOKEN_ERROR                       | 203  | token错误                                              |
| CONNECT_ROOM_FAILED               | 300  | 连接房间失败                                           |
| PAGE_INFO_TIMEOUT                 | 301  | 等待页数据下发超时                                     |
| BOARD_OPEN_TIMEOUT                | 400  | 打开白板超时                                           |
| PLAYBACK_ROOM_OFFLINE_NOT_COEXIST | 500  | 房间模式，回放模式，离线模式不能共存，必须先关闭另一个 |
| DOWNLOAD_RECORD_FILE_FAILED       | 501  | 下载回放记录文件失败                                   |
| RECORD_NOT_FOUND                  | 502  | 回放记录不存在                                         |
| RECORD_NOT_COMPLETE               | 503  | 录制未结束                                             |