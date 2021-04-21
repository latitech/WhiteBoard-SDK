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
    implementation 'com.latitech.android:whiteboard:0.0.3'

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

在有白板的activity的layout文件中引入[com.latitech.whiteboard.WhiteBoardView]控件，
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

首先构建进房参数[JoinConfig]，然后执行[joinRoom](#joinroom)来加入房间。

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

如果加入房间成功，会收到[onJoinSuccess]回调，如果加入失败会收到[onJoinFailed]回调。

## 关闭并离开房间

房间关闭时，比如离开房间的`Activity`时，必须调用[leaveRoom](#leaveroom)来退出房间并释放资源，
此方法会同时完成离开房间和资源释放，同时可以自动释放[AutoRemoveWhiteBoardListener]类型的事件监听器（推荐），多次执行是安全的。

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
|[setDefaultInputMode](#setdefaultinputmode)|设置白板的默认初始输入模式配置|
|[setRetry](#setretry)|设置白板断线自动重连次数|
|[setInputMode](#setinputmode)|改变白板的输入模式|
|[setBackgroundColor](#setbackgroundcolor)|设置白板背景色|
|[scroll](#scroll)|垂直滚动白板显示区|
|[newBoardPage](#newboardpage)|新建白板页|
|[insertBoardPage](#insertboardpage)|插入新白板页|
|[jumpBoardPage](#jumpboardpage)|跳转到目标白板页|
|[preBoardPage](#preboardpage)|后退到上一页|
|[nextBoardPage](#nextboardpage)|前进到下一页|
|[deleteBoardPage](#deleteboardpage)|删除白板页|
|[insertFile](#insertfile)|向当前白板页中插入文件|
|[jumpFilePage](#jumpfilepage)|文件翻页|
|deleteFile|删除文件|
|revert|撤销一次擦除的笔迹|
|[screenshots](#screenshots)|白板截图|

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

* 通过调用[WhiteBoard.addListener](#addlistener)可以添加事件监听器，可以在任何时期添加，包括进入白板之前。
* 所有的事件都在[WhiteBoardListener]接口中。如果添加此类的直接子类作为监听器，则添加后必须由用户手动调用[WhiteBoard.removeListener
](#removelistener)来移除。此方式通常用于在房间外监听房间中发生的事并记录日志或触发某些全局事件时使用。
* [WhiteBoardListener]存在一个易用的子类[AutoRemoveWhiteBoardListener]，如果添加此类的子类监听器，则用户在调用[WhiteBoard.leaveRoom
](#leaveroom)时系统会自动清理所有的[AutoRemoveWhiteBoardListener]子类监听器，无需用户手动移除。

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

# WhiteBoard

所有白板SDK的主动用户接口，所有接口线程安全

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

加入成功后本地会收到[onJoinSuccess]回调，加入失败则会收到[onJoinFailed]回调。
同时远端用户会收到[onUserJoin]回调。

如果已经加入成功但是发生了掉线，则SDK会尝试自动重连，并收到[onReconnecting]回调。
重连次数可以通过[setRetry](#setretry)修改，重连成功后会收到[onReconnected]，重连失败会收到[onDisconnected]，此时必须用户手动调用此方法重新加入房间。

|参数|描述|
|----|----|
|[config]|房间和身份信息|

## leaveRoom

`public static void leaveRoom()`

离开白板房间

该方法会断开白板连接并释放资源，同时会清理所有的[AutoRemoveWhiteBoardListener]类型的监听器。
多次调用是安全的。
离开白板后远端用户会收到[onUserLeave]回调。

## addListener

`public static void addListener(@NonNull WhiteBoardListener listener)`

添加一个白板事件监听器

如果添加[WhiteBoardListener]的直接子类则会永久存续，直到使用[removeListener](#removelistener)移除。 
如果添加[AutoRemoveWhiteBoardListener]的子类则可以在[leaveRoom](#leaveroom)时自动移除，无需手动执行[removeListener](#removelistener)。

可以在[joinRoom](#joinroom)之前或之后添加

|参数|描述|
|----|----|
|[listener]|事件监听器|

## removeListener

`public static void removeListener(@NonNull WhiteBoardListener listener)`

移除一个白板事件监听器

可在任何时候调用。

|参数|描述|
|----|----|
|[listener]|监听器实例|

## clearListener

`public static void clearListener()`

清空所有白板监听器

会清空所有的监听器，包括[WhiteBoardListener]和[AutoRemoveWhiteBoardListener]类型的全部监听器。

可在任何时候调用。

## screenshots

`public static void screenshots(@NonNull ScreenshotsCallback listener)`

白板截图

仅在[WhiteBoardView]附加到布局中时有效（即必须有可见的白板），回调[ScreenshotsCallback]将在非主线程执行。

|参数|描述|
|----|----|
|[listener]|截图回调，在非主线程回调，截图成功会返回`Bitmap`，失败返回null|

## setDefaultInputMode

`public static void setDefaultInputMode(@NonNull InputConfig config)`

设置白板的默认初始输入模式配置

在[joinRoom](#joinroom)之前调用有效，已经进入房间时调用此方法不会改变当前的输入模式，仅会影响下次进入房间的输入模式。
如需改变当前房间中的输入模式，请调用[setInputMode](#setinputmode)方法。

此方法用于预先设定加入房间后的初始输入配置，反复加入离开房间不会影响此默认设置。
默认设置不会随[setInputMode](#setinputmode)方法改变，即一次设定长期有效。

|参数|描述|
|----|----|
|[config]|输入模式配置|

## setRetry

`public static void setRetry(int count)`

设置白板断线自动重连次数

默认为10次，设为0表示不自动重连。

|参数|描述|
|----|----|
|count|重连次数|

## setInputMode

`public static void setInputMode(@NonNull InputConfig config)`

改变白板的输入模式

此方法用于已经连通白板房间时改变用户对白板的输入模式，包括例如笔迹的粗细和颜色等。

|参数|描述|
|----|----|
|[config]|输入模式配置|

## setBackgroundColor

`public static void setBackgroundColor(@ColorInt int color)`

设置白板背景色

用于在白板房间中改变当前白板页的背景色，未进入房间调用无效。

* 仅能改变当前页面的背景以及此后新增的页面背景，不会影响已经存在的页面背景。
* 如需设定新房间的初始背景色，请在服务器创建房间时设定。

|参数|描述|
|----|----|
|color|颜色值，不支持透明度|

## scroll

`public static void scroll(float offsetY)`

垂直滚动白板显示区

* 目前白板是一个纵向可滚动（高大于宽）的矩形，通常白板的可视区[WhiteBoardViewport]不能呈现完整白板，需要通过滚动来控制可见区。
* 白板内部可以通过用户的双指操作来滚动白板，无需外部干涉，此方法的目的是方便用户实现类似top按钮或滚动条功能。

无论是调用此方法还是用户通过白板手势滚动了白板（包括远程用户滚动白板），都会触发[onBoardScroll]回调。

|参数|描述|
|----|----|
|offsetY|白板的垂直偏移量，此值为总量而非增量，当前值在[WhiteBoardViewport]中描述|

## newBoardPage

`public static void newBoardPage()`

新增白板页

在房间中调用可以在当前页列表末尾插入一个新的页面并会自动跳转到这个新页面。

页面创建成功后用户会收到[onCurrentBoardPageChanged]、[onBoardPageList]、[onBoardPageInfoChanged]三个回调。

## insertBoardPage

`public static void insertBoardPage(@NonNull String pageId)`

插入新白板页

在指定的页面之前插入一个新白板页，同时白板会自动跳转到新插入的页面。

页面创建成功后用户会收到[onCurrentBoardPageChanged]、[onBoardPageList]、[onBoardPageInfoChanged]三个回调。

|参数|描述|
|----|----|
|pageId|目标插入位置的页id，此id来自于页数据，可通过[getPageList]或[getCurrentPage]获取页列表或当前显示页数据，也可通过[onBoardPageList]和[onCurrentBoardPageChanged]回调来收集|

## jumpBoardPage

`public static void jumpBoardPage(@NonNull String pageId)`

跳转到指定白板页

直接跳页的实现方式。

跳转成功后用户会收到[onCurrentBoardPageChanged]、[onBoardPageInfoChanged]回调。

|参数|描述|
|----|----|
|pageId|跳转目标的页id，此id来自于页数据，可通过[getPageList]或[getCurrentPage]获取页列表或当前显示页数据，也可通过[onBoardPageList]和[onCurrentBoardPageChanged]回调来收集|

## preBoardPage

`public static void preBoardPage()`

返回到上一页

成功后用户会收到[onCurrentBoardPageChanged]、[onBoardPageInfoChanged]回调。

## nextBoardPage

`public static void nextBoardPage()`

前进到下一页

成功后用户会收到[onCurrentBoardPageChanged]、[onBoardPageInfoChanged]回调。

## deleteBoardPage

`public static void deleteBoardPage(@NonNull String pageId)`

删除白板页

删除成功后一定会收到[onBoardPageList]回调，如果删除的是当前页会同时触发[onCurrentBoardPageChanged]，
如果删除当前页是仅有的一页，白板会删除当前页并立即创建一个新的空白页，效果类似与清空白板。

|参数|描述|
|----|----|
|pageId|要删除的页id，此id来自于页数据，可通过[getPageList]或[getCurrentPage]获取页列表或当前显示页数据，也可通过[onBoardPageList]和[onCurrentBoardPageChanged]回调来收集|

## insertFile

`public static void insertFile(@NonNull FileConfig config)`

向当前白板页中插入文件

支持的格式有jpg,png,pdf,doc,docx,ppt,pptx,xls,xlsx。
图片尽量上传2K及以下的尺寸，否则某些老旧设备可能无法加载。
office文件需要在线转换格式，所以画面呈现会相对慢一些。

文件插入后会收到大量[onWidgetActionEvent]回调（与房间人数有关），此回调仅表达了导致文件状态变化的用户信息和此用户的加载情况，
通常用于在界面上表达房间中各成员对此文件的加载状态。

|参数|描述|
|----|----|
|[config]|文件配置信息|

## jumpFilePage

`public static void jumpFilePage(@NonNull String widgetId , int pageNo)`

文件翻页

对于可翻页的文件，如pdf和office文件，通过调用此方法可以使文件跳到指定序号的页面。

翻页成功后会收到[onFilePageChanged]回调。

|参数|描述|
|----|----|
|widgetId|文件的widgetId，每个文件都有一个id，可以通过[getActiveWidget]方法获取当前用户正在操作的Widget，也可以通过[onWidgetActive]收集当前正在操作的Widget|
|pageNo|跳转的目标页号，从1开始，如果序号超出文件范围，跳转会失败并忽略|



