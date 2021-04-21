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
    implementation 'com.latitech.android:whiteboard:0.0.2'

    // 可选，如果项目使用了androidx可以添加此项开启sdk的可空/非空参数注解的识别，在kotlin环境非常有用。
    compileOnly 'androidx.annotation:annotation:1.1.0'
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
}

```

## 初始化SDK

* SDK需要在`Application`中初始化一次

```init

class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        WhiteBoard.init(this)
    }
}

```

其中[WhiteBoard](#whiteboard)是所有用户主动方法的接口类。

## 在布局中引入白板控件

在有白板的activity的layout文件中引入`com.latitech.whiteboard.WhiteBoardView`控件，
控件大小最好与用户设定的白板宽高比相同，否则边缘可能会留白。白板大小由用户服务器创建房间时设定。

例如：

```layout

<androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

    ...

    <com.latitech.whiteboard.WhiteBoardView
                android:id="@+id/white_board"
                android:layout_width="0dp"
                android:layout_height="0dp"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintDimensionRatio="2048:1440"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@+id/horizontalScrollView" />

</androidx.constraintlayout.widget.ConstraintLayout>

```

示例中通过`app:layout_constraintDimensionRatio`参数设定宽高比，比例中的数字时白板的虚拟大小，决定了白板中内容的坐标系。

## 加入房间

首先访问自己的服务器获取要加入的白板房间的roomId,token,appId等参数（房间的创建和token的生成由服务器对接SDK服务端接口）。

首先构建进房参数`JoinConfig`，然后执行[joinRoom](#joinroom)来加入房间。

```joinRoom

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

如果加入房间成功，会收到`onJoinSuccess`回调，如果加入失败会收到`onJoinFailed`回调。

## 关闭并离开房间

房间关闭时，比如离开房间的`Activity`时，必须调用[leaveRoom](#leaveroom)来退出房间并释放资源，
此方法会同时完成离开房间和资源释放，同时可以自动释放`AutoRemoveWhiteBoardListener`类型的事件监听器（推荐），多次执行是安全的。

通常情况会把它放在`Activity.onDestroy`中执行，比如：

```leaveRoom

    override fun onDestroy() {
        WhiteBoard.leaveRoom()
        super.onDestroy()
    }

```

## 关键方法

* 所有用户可以主动调用的方法都是[WhiteBoard](#whiteboard)类的静态方法

|方法名称|方法描述|
|----|----|
|[init](#init)|初始化白板SDK|
|[joinRoom](#joinroom)|进入白板房间|
|[leaveRoom](#leaveroom)|离开白板房间|
|[addListener](#addlistener)|添加一个白板事件监听器|
|[removeListener](#removelistener)|移除一个白板事件监听器|
|[clearListener](#clearlistener)|清空所有白板监听器|

## 主动控制的方法

* 所有用户可以主动调用的方法都是[WhiteBoard](#whiteboard)类的静态方法

|方法名称|方法描述|
|----|----|
|setDefaultInputMode|设置白板的默认初始输入模式配置|
|setRetry|设置白板断线自动重连次数|
|setInputMode|改变白板的输入模式|
|setBackgroundColor|设置白板背景色|
|scroll|垂直滚动白板显示区|
|newBoardPage|新建白板页|
|insertBoardPage|插入新白板页|
|jumpBoardPage|跳转到目标白板页|
|preBoardPage|后退到上一页|
|nextBoardPage|前进到下一页|
|deleteBoardPage|删除白板页|
|insertFile|向当前白板页中插入文件|
|jumpFilePage|文件翻页|
|deleteFile|删除文件|
|revert|撤销一次擦除的笔迹|
|screenshots|白板截图|

## 获取白板当前属性的方法

* 所有用户可以主动调用的方法都是[WhiteBoard](#whiteboard)类的静态方法

|方法名称|方法描述|
|----|----|
|getStatus|获取白板当前状态|
|getRoom|获取当前加入的房间信息|
|getMe|获取当前房间中的个人信息|
|getUsers|获取当前房间中的用户列表|
|getPageList|获取当前白板的全部页信息列表|
|getCurrentPage|获取当前白板页信息|
|getBackgroundColor|获取当前显示的白板背景色|
|getInputConfig|获取当前使用的白板输入模式|
|getActiveWidget|获取当前被激活操作的widget|
|canRecovery|是否存在可还原的笔迹|
|getViewport|获取当前白板的窗口尺寸信息|

## 添加事件监听器

* 通过调用`WhiteBoard.addListener`可以添加事件监听器，可以在任何时期添加，包括进入白板之前。
* 所有的事件都在`WhiteBoardListener`接口中。如果添加此类的直接子类作为监听器，则添加后必须由用户手动调用`WhiteBoard.removeListener`来移除。此方式通常用于在房间外监听房间中发生的事并记录日志或触发某些全局事件时使用。
* `WhiteBoardListener`存在一个易用的子类`AutoRemoveWhiteBoardListener`，如果添加此类的子类监听器，则用户在调用`WhiteBoard.leaveRoom`时系统会自动清理所有的`AutoRemoveWhiteBoardListener`子类监听器，无需用户手动移除。

|事件名称|事件描述|
|----|----|
|onJoinSuccess|成功加入白板房间|
|onJoinFailed|加入房间失败|
|onReconnecting|白板正在自动重连|
|onReconnected|自动重连成功|
|onDisconnected|房间彻底断开连接|
|onBoardStatusChanged|白板房间状态变化|
|onUserList|当前已经在房间中的用户列表|
|onUserJoin|有其它用户加入了房间|
|onUserLeave|有其它用户离开了房间|
|onBoardPageList|白板页信息列表|
|onCurrentBoardPageChanged|白板当前页变化|
|onBoardPageInfoChanged|某一个白板页信息变化|
|onBoardSizeChanged|白板的虚拟大小发生变化|
|onBoardScroll|白板内发生滚动|
|onBackgroundColorChanged|白板背景色变化|
|onWidgetActive|有widget被激活|
|onFilePageChanged|文件被翻页|
|onWidgetActionEvent|widget被执行了某些关键动作|
|onRecoveryStateChanged|笔迹回收站状态变化|

# WhiteBoard类

所有白板SDK的主要用户接口，所有接口线程安全

## init

`public static void init(@NonNull Context context , boolean debug)`

初始化白板SDK

应用启动后只需调用一次，最好在`Application`中调用。

|参数|描述|
|----|----|
|context|Android上下文|
|debug|是否开启debug模式，如果为true则sdk会输出一些日志|

## joinRoom

`public static void joinRoom(@NonNull JoinConfig config)`

进入白板房间

只有此方法执行成功才能连通白板，目前sdk仅支持同时进入一个房间，多次调用是安全的，但仅有第一次调用的参数有效，后续调用会被忽略，如需进入其它房间，需要先执行[leaveRoom](#leaveroom)。

加入成功后本地会收到onJoinSuccess回调，加入失败则会收到onJoinFailed回调。
同时远端用户会收到onUserJoin回调。

如果已经加入成功但是发生了掉线，则SDK会尝试自动重连，并收到onReconnecting回调。
重连次数可以通过setRetry修改，重连成功后会收到onReconnected，重连失败会收到onDisconnected，此时必须用户手动调用此方法重新加入房间。

|参数|描述|
|----|----|
|config|房间和身份信息|

## leaveRoom

`public static void leaveRoom()`

离开白板房间

该方法会断开白板连接并释放资源，同时会清理所有的AutoRemoveWhiteBoardListener类型的监听器。
多次调用是安全的。
离开白板后远端用户会收到onUserLeave回调。

## addListener

`public static void addListener(@NonNull WhiteBoardListener listener)`

添加一个白板事件监听器

如果添加WhiteBoardListener的直接子类则会永久存续，直到使用removeListener移除。 
如果添加AutoRemoveWhiteBoardListener的子类则可以在[leaveRoom](#leaveroom)时自动移除，无需手动执行removeListener。

可以在[joinRoom](#joinroom)之前或之后添加

|参数|描述|
|----|----|
|listener|事件监听器|

## removeListener

`public static void removeListener(@NonNull WhiteBoardListener listener)`

移除一个白板事件监听器

可在任何时候调用。

|参数|描述|
|----|----|
|listener|监听器实例|

## clearListener

`public static void clearListener()`

清空所有白板监听器

会清空所有的监听器，包括WhiteBoardListener和AutoRemoveWhiteBoardListener类型的全部监听器。

可在任何时候调用。

