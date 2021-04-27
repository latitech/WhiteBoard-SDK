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
    implementation 'com.latitech.android:whiteboard:0.1.0'

    // 可选，如果项目使用了androidx可以添加此项开启sdk的可空/非空参数注解的识别，在kotlin环境非常有用。
    compileOnly 'androidx.annotation:annotation:1.2.0'
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

在有白板的activity的layout文件中引入[com.latitech.whiteboard.WhiteBoardView](#whiteboardview)控件，
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

示例中通过`app:layout_constraintDimensionRatio`参数设定宽高比，比例中的数字是白板的虚拟大小，决定了白板中内容的坐标系。

## 加入房间

首先访问自己的服务器获取要加入的白板房间的roomId,token,appId等参数（房间的创建和token的生成由服务器对接SDK服务端接口）。

首先构建进房参数[JoinConfig](#joinconfig)，然后执行[joinRoom](#joinroom)来加入房间。

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

如果加入房间成功，会收到[onJoinSuccess](#onjoinsuccess)回调，如果加入失败会收到[onJoinFailed](#onjoinfailed)回调。

## 关闭并离开房间

房间关闭时，比如离开房间的`Activity`时，必须调用[leaveRoom](#leaveroom)来退出房间并释放资源，
此方法会同时完成离开房间和资源释放，同时可以自动释放[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)类型的事件监听器（推荐），多次执行是安全的。

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
|[deleteFile](#deletefile)|删除文件|
|[recover](#recover)|撤销一次擦除的笔迹|
|[screenshots](#screenshots)|白板截图|

## 获取白板当前属性的方法

* 所有用户可以主动调用的方法都是[WhiteBoard](#whiteboard)类的静态方法

|方法名称|方法描述|
|----|----|
|[getStatus](#getstatus)|获取白板当前状态|
|[getRoom](#getroom)|获取当前加入的房间信息|
|[getMe](#getme)|获取当前房间中的个人信息|
|[getUsers](#getusers)|获取当前房间中的用户列表|
|[getPageList](#getpagelist)|获取当前白板的全部页信息列表|
|[getCurrentPage](#getcurrentpage)|获取当前白板页信息|
|[getBackgroundColor](#getbackgroundcolor)|获取当前显示的白板背景色|
|[getInputConfig](#getinputconfig)|获取当前使用的白板输入模式|
|[getActiveWidget](#getactivewidget)|获取当前被激活操作的widget|
|[canRecovery](#canrecovery)|是否存在可还原的笔迹|
|[getViewport](#getviewport)|获取当前白板的窗口尺寸信息|

## 添加事件监听器

* 通过调用[WhiteBoard.addListener](#addlistener)可以添加事件监听器，可以在任何时期添加，包括进入白板之前。
* 所有的事件都在[WhiteBoardListener](#whiteboardlistener)接口中。如果添加此类的直接子类作为监听器，则添加后必须由用户手动调用[WhiteBoard.removeListener](#removelistener)来移除。此方式通常用于在房间外监听房间中发生的事并记录日志或触发某些全局事件时使用。
* [WhiteBoardListener](#whiteboardlistener)存在一个易用的子类[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)，如果添加此类的子类监听器，则用户在调用[WhiteBoard.leaveRoom](#leaveroom)时系统会自动清理所有的[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)子类监听器，无需用户手动移除。

|事件名称|事件描述|
|----|----|
|[onJoinSuccess](#onjoinsuccess)|成功加入白板房间|
|[onJoinFailed](#onjoinfailed)|加入房间失败|
|[onReconnecting](#onreconnecting)|白板正在自动重连|
|[onReconnected](#onreconnected)|自动重连成功|
|[onDisconnected](#ondisconnected)|房间彻底断开连接|
|[onBoardStatusChanged](#onboardstatuschanged)|白板房间状态变化|
|[onUserList](#onuserlist)|当前已经在房间中的用户列表|
|[onUserJoin](#onuserjoin)|有其它用户加入了房间|
|[onUserLeave](#onuserleave)|有其它用户离开了房间|
|[onBoardPageList](#onboardpagelist)|白板页信息列表|
|[onCurrentBoardPageChanged](#oncurrentboardpagechanged)|白板当前页变化|
|[onBoardPageInfoChanged](#onboardpageinfochanged)|某一个白板页信息变化|
|[onBoardSizeChanged](#onboardsizechanged)|白板的虚拟大小发生变化|
|[onBoardScroll](#onboardscroll)|白板内发生滚动|
|[onBackgroundColorChanged](#onbackgroundcolorchanged)|白板背景色变化|
|[onWidgetActive](#onwidgetactive)|有widget被激活|
|[onFilePageChanged](#onfilepagechanged)|文件被翻页|
|[onWidgetActionEvent](#onwidgetactionevent)|widget被执行了某些关键动作|
|[onRecoveryStateChanged](#onrecoverystatechanged)|笔迹回收站状态变化|


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

加入成功后本地会收到[onJoinSuccess](#onjoinsuccess)回调，加入失败则会收到[onJoinFailed](#onjoinfailed)回调。
同时远端用户会收到[onUserJoin](#onuserjoin)回调。

如果已经加入成功但是发生了掉线，则SDK会尝试自动重连，并收到[onReconnecting](#onreconnecting)回调。
重连次数可以通过[setRetry](#setretry)修改，重连成功后会收到[onReconnected](#onreconnected)，重连失败会收到[onDisconnected](#ondisconnected)，此时必须用户手动调用此方法重新加入房间。

|参数|描述|
|----|----|
|[config](#joinconfig)|房间和身份信息|

## leaveRoom

`public static void leaveRoom()`

离开白板房间

该方法会断开白板连接并释放资源，同时会清理所有的[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)类型的监听器。
多次调用是安全的。
离开白板后远端用户会收到[onUserLeave](#onuserleave)回调。

## addListener

`public static void addListener(@NonNull WhiteBoardListener listener)`

添加一个白板事件监听器

如果添加[WhiteBoardListener](#whiteboardlistener)的直接子类则会永久存续，直到使用[removeListener](#removelistener)移除。 
如果添加[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)的子类则可以在[leaveRoom](#leaveroom)时自动移除，无需手动执行[removeListener](#removelistener)。

可以在[joinRoom](#joinroom)之前或之后添加

|参数|描述|
|----|----|
|[listener](#whiteboardlistener)|事件监听器|

## removeListener

`public static void removeListener(@NonNull WhiteBoardListener listener)`

移除一个白板事件监听器

可在任何时候调用。

|参数|描述|
|----|----|
|[listener](#whiteboardlistener)|监听器实例|

## clearListener

`public static void clearListener()`

清空所有白板监听器

会清空所有的监听器，包括[WhiteBoardListener](#whiteboardlistener)和[AutoRemoveWhiteBoardListener](#autoremovewhiteboardlistener)类型的全部监听器。

可在任何时候调用。

## screenshots

`public static void screenshots(@NonNull ScreenshotsCallback listener)`

白板截图

仅在[WhiteBoardView](#whiteboardview)附加到布局中时有效（即必须有可见的白板），回调[ScreenshotsCallback](#screenshotscallback)将在非主线程执行。

|参数|描述|
|----|----|
|[listener](#whiteboardlistener)|截图回调，在非主线程回调，截图成功会返回`Bitmap`，失败返回null|

## setDefaultInputMode

`public static void setDefaultInputMode(@NonNull InputConfig config)`

设置白板的默认初始输入模式配置

在[joinRoom](#joinroom)之前调用有效，已经进入房间时调用此方法不会改变当前的输入模式，仅会影响下次进入房间的输入模式。
如需改变当前房间中的输入模式，请调用[setInputMode](#setinputmode)方法。

此方法用于预先设定加入房间后的初始输入配置，反复加入离开房间不会影响此默认设置。
默认设置不会随[setInputMode](#setinputmode)方法改变，即一次设定长期有效。

|参数|描述|
|----|----|
|[config](#inputconfig)|输入模式配置|

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
|[config](#inputconfig)|输入模式配置|

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

* 目前白板是一个纵向可滚动（高大于宽）的矩形，通常白板的可视区[WhiteBoardViewport](#whiteboardviewport)不能呈现完整白板，需要通过滚动来控制可见区。
* 白板内部可以通过用户的双指操作来滚动白板，无需外部干涉，此方法的目的是方便用户实现类似top按钮或滚动条功能。

无论是调用此方法还是用户通过白板手势滚动了白板（包括远程用户滚动白板），都会触发[onBoardScroll](#onboardscroll)回调。

|参数|描述|
|----|----|
|offsetY|白板的垂直偏移量，此值为总量而非增量，当前值在[WhiteBoardViewport](#whiteboardviewport)中描述|

## newBoardPage

`public static void newBoardPage()`

新增白板页

在房间中调用可以在当前页列表末尾插入一个新的页面并会自动跳转到这个新页面。

页面创建成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageList](#onboardpagelist)、[onBoardPageInfoChanged](#onboardpageinfochanged)三个回调。

## insertBoardPage

`public static void insertBoardPage(@NonNull String pageId)`

插入新白板页

在指定的页面之前插入一个新白板页，同时白板会自动跳转到新插入的页面。

页面创建成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageList](#onboardpagelist)、[onBoardPageInfoChanged](#onboardpageinfochanged)三个回调。

|参数|描述|
|----|----|
|pageId|目标插入位置的页id，此id来自于页数据，可通过[getPageList](#getpagelist)或[getCurrentPage](#getcurrentpage)获取页列表或当前显示页数据，也可通过[onBoardPageList](#onboardpagelist)和[onCurrentBoardPageChanged](#oncurrentboardpagechanged)回调来收集|

## jumpBoardPage

`public static void jumpBoardPage(@NonNull String pageId)`

跳转到指定白板页

直接跳页的实现方式。

跳转成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageInfoChanged](#onboardpageinfochanged)回调。

|参数|描述|
|----|----|
|pageId|跳转目标的页id，此id来自于页数据，可通过[getPageList](#getpagelist)或[getCurrentPage](#getcurrentpage)获取页列表或当前显示页数据，也可通过[onBoardPageList](#onboardpagelist)和[onCurrentBoardPageChanged](#oncurrentboardpagechanged)回调来收集|

## preBoardPage

`public static void preBoardPage()`

返回到上一页

成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageInfoChanged](#onboardpageinfochanged)回调。

## nextBoardPage

`public static void nextBoardPage()`

前进到下一页

成功后用户会收到[onCurrentBoardPageChanged](#oncurrentboardpagechanged)、[onBoardPageInfoChanged](#onboardpageinfochanged)回调。

## deleteBoardPage

`public static void deleteBoardPage(@NonNull String pageId)`

删除白板页

删除成功后一定会收到[onBoardPageList](#onboardpagelist)回调，如果删除的是当前页会同时触发[onCurrentBoardPageChanged](#oncurrentboardpagechanged)，
如果删除当前页是仅有的一页，白板会删除当前页并立即创建一个新的空白页，效果类似与清空白板。

|参数|描述|
|----|----|
|pageId|要删除的页id，此id来自于页数据，可通过[getPageList](#getpagelist)或[getCurrentPage](#getcurrentpage)获取页列表或当前显示页数据，也可通过[onBoardPageList](#onboardpagelist)和[onCurrentBoardPageChanged](#oncurrentboardpagechanged)回调来收集|

## insertFile

`public static void insertFile(@NonNull FileConfig config)`

向当前白板页中插入文件

支持的格式有图片jpg,png
文档pdf,doc,docx,dot,wps,wpt,dotx,docm,dotm,rtf
演示文稿ppt,pptx,pot,potx,pps,ppsx,dps,dpt,pptm,potm,ppsm
表格xls,xlsx,xlt,et,ett,xltx,csv,xlsb,xlsm,xltm
如果试图插入不支持的文件类型则会被忽略。

图片尽量上传2K及以下的尺寸，否则某些老旧设备可能无法加载。
office文件需要在线转换格式，所以画面呈现会相对慢一些。

文件插入后会收到大量[onWidgetActionEvent](#onwidgetactionevent)回调（与房间人数有关），此回调仅表达了导致文件状态变化的用户信息和此用户的加载情况，
通常用于在界面上表达房间中各成员对此文件的加载状态。

|参数|描述|
|----|----|
|[config](#fileconfig)|文件配置信息|

## jumpFilePage

`public static void jumpFilePage(@NonNull String widgetId , int pageNo)`

文件翻页

对于可翻页的文件，如pdf和office文件，通过调用此方法可以使文件跳到指定序号的页面。

翻页成功后会收到[onFilePageChanged](#onfilepagechanged)回调。

|参数|描述|
|----|----|
|widgetId|文件的widgetId，每个文件都有一个id，可以通过[getActiveWidget](#getactivewidget)方法获取当前用户正在操作的Widget，也可以通过[onWidgetActive](#onwidgetactive)收集当前正在操作的Widget|
|pageNo|跳转的目标页号，从1开始，如果序号超出文件范围，跳转会失败并忽略|

## deleteFile

`public static void deleteFile(@NonNull String widgetId)`

删除文件

删除后会触发[onWidgetActionEvent](#onwidgetactionevent)回调，如果删除的文件是当前正在激活的widget，则会收到[onWidgetActive](#onwidgetactive)回调，并且参数为null。

|参数|描述|
|----|----|
|widgetId|文件的widgetId，每个文件都有一个id，可以通过[getActiveWidget](#getactivewidget)方法获取当前用户正在操作的Widget，也可以通过[onWidgetActive](#onwidgetactive)收集当前正在操作的Widget|

## recover

`public static void recover()`

还原最近一次擦除的笔迹

在输入模式为橡皮模式[InputConfig.erase](#inputconfigerase)时，本用户擦除的笔迹可以通过调用此方法来还原（回滚）。
一次擦除的笔迹指的是用户从落下手指移动擦除线条到抬起手指为止期间擦掉的所有线条。

* 当切换到其它输入模式或者白板翻页后擦除的笔迹缓存将会清空，将无法再还原擦掉的笔迹，即此方法仅在[InputConfig.erase](#inputconfigerase)模式下有效。
* 此方法多次调用是安全的。
* 判断当前是否有可还原的笔迹可以通过调用[canRecovery](#canrecovery)或监听[onRecoveryStateChanged](#onrecoverystatechanged)回调获知。

## getStatus

`@NonNull public static BoardStatus getStatus()`

获取白板当前状态

也可以监听[onBoardStatusChanged](#onboardstatuschanged)。

- 返回 
    - 当前的白板状态[BoardStatus](#boardstatus)。

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
    
## getPageList

`@NonNull public static List<WhiteBoardPage> getPageList()`

获取当前白板的全部页信息列表

此列表与监听[onBoardPageList](#onboardpagelist)，[onBoardPageInfoChanged](#onboardpageinfochanged)处理后获得的列表一致。

- 返回
    - 一个只读的[WhiteBoardPage](#whiteboardpage)白板页信息列表，如果未加入房间则会返回空列表。
    
## getCurrentPage

`@Nullable public static WhiteBoardPage getCurrentPage()`

获取当前显示的白板页信息

此信息与监听[onCurrentBoardPageChanged](#oncurrentboardpagechanged)获得的信息一致。

- 返回
    - 当前白板页信息[WhiteBoardPage](#whiteboardpage)，如果未加入房间则会返回null。
    
## getBackgroundColor

`@ColorInt public static int getBackgroundColor()`

获取当前白板页的背景色

此值与监听[onBackgroundColorChanged](#onbackgroundcolorchanged)获得的颜色一致。
可通过调用[setBackgroundColor](#setbackgroundcolor)改变当前白板页的背景。
默认背景色由服务器创建房间时指定。

- 返回
    - 当前白板页颜色值，如果未加入房间则会返回一个固定值`Color.LTGRAY`。
    
## getInputConfig

`@NonNull public static InputConfig getInputConfig()`

获取白板当前的输入模式

- 返回
    - 通过[setInputMode](#setinputmode)设置的[InputConfig](#inputconfig)，如果未加入房间则会返回默认配置，默认值可通过[setDefaultInputMode](#setdefaultinputmode)设置。
    
## getActiveWidget

`@Nullable public static ActiveWidgetInfo getActiveWidget()`

获取当前被激活操作的widget信息

此信息与监听[onWidgetActive](#onwidgetactive)获得的数据一致。

- 返回
    - [ActiveWidgetInfo](#activewidgetinfo)，如果当前用户没有操作过任何widget或者用户未加入房间则会返回null。
    
## canRecovery

`public static boolean canRecovery()`

是否存在可还原的笔迹（擦除还原，仅对笔迹有效）

此值与监听[onRecoveryStateChanged](#onrecoverystatechanged)回调获取的值一致。

- 返回
    - 如果为true表示有可还原的笔迹，此时可通过调用[recover](#recover)来还原一次擦除操作。false时调用[recover](#recover)无效。
    
## getViewport

`@NonNull public static WhiteBoardViewport getViewport()`

获取当前白板的可视区信息，包括白板的大小和偏移

此数据与监听[onBoardSizeChanged](#onboardsizechanged)和[onBoardScroll](#onboardscroll)获取的数据一致。
滚动白板可由用户双指手势拖动白板，也可通过程序主动调用[scroll](#scroll)完成。

- 返回
    - 白板的可视区[WhiteBoardViewport](#whiteboardviewport)，通常此值是跟随用户滚动白板而变化，如果未加入房间则会返回固定值[WhiteBoardViewport.IDLE](#whiteboardviewportidle)。
    

# WhiteBoardListener

所有的白板事件的监听器，所有事件响应均在主线程回调。
可通过[addListener](#addlistener)添加。

# AutoRemoveWhiteBoardListener

`public interface AutoRemoveWhiteBoardListener extends WhiteBoardListener`

能自动移除的白板事件监听器（推荐使用）
此监听器同样可以在任何时期添加，但是会伴随[leaveRoom](#leaveroom)的调用而自动移除，无需用户手动执行[removeListener](#removelistener)。

## onJoinSuccess

`void onJoinSuccess(@NonNull Room room , @NonNull RoomMember me)`

成功加入白板房间

[joinRoom](#joinroom)成功后的第一个关键事件（[onBoardStatusChanged](#onboardstatuschanged)返回[BoardStatus.SUCCESSFUL](#boardstatussuccessful)会先一步触发）。
在这里可以处理一些加入房间成功时的初始化工作。
* 在断线重连成功时同样会触发此事件，之后才会触发[onReconnected](#onreconnected)事件。

|参数|描述|
|----|----|
|[room](#room)|房间信息|
|[me](#roommember)|个人信息，由[joinRoom](#joinroom)传递的[JoinConfig](#joinconfig)中携带的信息|

## onJoinFailed

`void onJoinFailed(int errorCode)`

加入房间失败

[joinRoom](#joinroom)执行失败后触发，因为并没有成功加入房间，所以不会执行自动重连。
失败后需要用户重新调用[joinRoom](#joinroom)来加入房间。
如果已经成功加入了房间但是发生了断线则会触发[onReconnecting](#onreconnecting)尝试自动重连，重连失败则会触发[onDisconnected](#ondisconnected)，而不会触发本事件。

|参数|描述|
|----|----|
|[errorCode](#whiteboarderrorcode)|失败错误码|

## onReconnecting

`void onReconnecting(int times)`

白板正在自动重连

当白板连接意外断开比如网络波动等，白板会自动尝试重连，在每次尝试开始时会触发此事件。
重连成功后会先触发[onJoinSuccess](#onjoinsuccess)后触发[onReconnected](#onreconnected)，
重连次数达到上限后会触发失败事件[onDisconnected](#ondisconnected)。
首次调用[joinRoom](#joinroom)失败不会自动重连，而是触发[onJoinFailed](#onjoinfailed)。

重连次数默认10次，可通过[setRetry](#setretry)修改。

|参数|描述|
|----|----|
|times|当前为第几次重试|

## onReconnected

`void onReconnected()`

自动重连成功

参考[onReconnecting](#onreconnecting)。

## onDisconnected

`void onDisconnected()`

自动重连失败，白板彻底断开连接

即在[onReconnecting](#onreconnecting)重试次数达到上限后触发，
此时需要用户重新执行[joinRoom](#joinroom)加入房间。

## onBoardStatusChanged

`void onBoardStatusChanged(@NonNull BoardStatus status)`

白板房间状态变化

从[joinRoom](#joinroom)到[leaveRoom](#leaveroom)之间，只要白板房间的状态发生变化就会触发此事件。
同时此事件触发早于[onJoinSuccess](#onjoinsuccess)，[onJoinFailed](#onjoinfailed)，[onReconnecting](#onreconnecting)等独立事件。
比如调用[joinRoom](#joinroom)后会立即触发[BoardStatus.LOADING](#boardstatusloading)的变化，[onJoinSuccess](#onjoinsuccess)触发之前会先触发[BoardStatus.SUCCESSFUL](#boardstatussuccessful)的变化。

|参数|描述|
|----|----|
|[status](#boardstatus)|新的白板状态|

## onUserList

`void onUserList(@NonNull List<RoomMember> users)`

当前已经在房间中的用户列表（包括自己）

加入房间后会触发一次返回已经在房间中的用户，自动重连成功后也会触发。

* 如果自己管理用户列表，需要以此回调的作为列表初始数据，并且在重连成功后重置初始列表。
* 后续的远端用户进出事件由[onUserJoin](#onuserjoin)和[onUserLeave](#onuserleave)反馈。
* [getUsers](#getusers)总是获取当前的完整用户列表。

|参数|描述|
|----|----|
|users|已经在房间中的用户信息[RoomMember](#roommember)列表，此列表为只读列表|

## onUserJoin

`void onUserJoin(@NonNull RoomMember user)`

有远端用户加入了房间

* 如果自己维护用户列表，注意更新列表数据

|参数|描述|
|----|----|
|[user](#roommember)|用户信息|

## onUserLeave

`void onUserLeave(@NonNull RoomMember user)`

有远端用户离开了房间

* 如果自己维护用户列表，注意更新列表数据

|参数|描述|
|----|----|
|[user](#roommember)|用户信息|

## onBoardPageList

`void onBoardPageList(@NonNull List<WhiteBoardPage> list)`

白板页信息列表

在首次进入房间和白板页列表结构变化时触发，比如新增页，删除页等等。
仅翻页不会触发此事件。

|参数|描述|
|----|----|
|list|白板页信息[WhiteBoardPage](#whiteboardpage)的只读列表，也可以通过[getPageList](#getpagelist)获得|

## onCurrentBoardPageChanged

`void onCurrentBoardPageChanged(@NonNull WhiteBoardPage page)`

白板当前页改变

在首次加入房间后和翻页时触发，新增页由于会自动切换到新页面，所以也会触发。

|参数|描述|
|----|----|
|[page](#whiteboardpage)|当前显示的白板页信息，也可以通过[getCurrentPage](#getcurrentpage)获得|

## onBoardPageInfoChanged

`void onBoardPageInfoChanged(@NonNull WhiteBoardPage page)`

某一个白板页信息变化

目前仅白板页的缩略图地址发生变化时才会触发此事件，每当白板发生翻页时都会自动更新上一个页面的缩略图，
所以通常情况下此事件触发的白板页信息不是当前正在显示的页面。

* 由于页号[WhiteBoardPage.pageNumber](#whiteboardpage)的变化是新增和删除页导致，可能同时影响大量的页信息，所以页号变化没有单独的事件，只能监听[onBoardPageList](#onboardpagelist)观察整个列表的变化。

|参数|描述|
|----|----|
|[page](#whiteboardpage)|有参数变化的新的页信息，当前仅有缩略图地址变化[WhiteBoardPage.thumbnails](#whiteboardpage)|

## onBoardSizeChanged

`void onBoardSizeChanged(@NonNull WhiteBoardViewport viewport)`

白板大小变化（虚拟大小）

在白板设定的虚拟大小变化时触发，首次进入白板一定会触发一次。

|参数|描述|
|----|----|
|[viewport](#whiteboardviewport)|白板的可视区数据|

## onBoardScroll

`void onBoardScroll(@NonNull WhiteBoardViewport viewport)`

白板滚动

白板内发生滚动时触发，主动调用[scroll](#scroll)也会触发，首次进入白板也会触发。

|参数|描述|
|----|----|
|[viewport](#whiteboardviewport)|白板的可视区数据|

## onBackgroundColorChanged

`void onBackgroundColorChanged(@ColorInt int backgroundColor)`

白板背景色改变

当前的白板背景色变化时触发，首次进入白板也会触发。
可通过[setBackgroundColor](#setbackgroundcolor)随时改变背景色。

|参数|描述|
|----|----|
|backgroundColor|新的颜色值|

## onWidgetActive

`void onWidgetActive(@Nullable ActiveWidgetInfo info)`

有新的widget被激活

用户书写或操作白板时会激活被操作的widget，同时触发此事件，比如移动文件时会收到被移动的文件信息，在白板上写字时会收到白板的信息。
当此widget是文件且此文件发生翻页后会再次触发此事件，同时也会触发[onFilePageChanged](#onfilepagechanged)。

|参数|描述|
|----|----|
|[info](#activewidgetinfo)|当前激活的widget信息，null表示用户还没有操作，比如刚刚翻页后|

## onFilePageChanged

`void onFilePageChanged(@NonNull ActiveWidgetInfo info)`

文件页改变

当文件被翻页时触发，同时会触发[onWidgetActive](#onwidgetactive)。
当前仅被激活的文件发生翻页时才会收到此事件。
文件翻页可通过调用[jumpFilePage](#jumpfilepage)实现。

|参数|描述|
|----|----|
|[info](#activewidgetinfo)|新的widget信息|

## onWidgetActionEvent

`void onWidgetActionEvent(@NonNull WidgetActionEvent event)`

widget被执行了某些关键动作

比如有人插入文件或删除文件会收到此事件。
同时每个远端用户的文件加载情况也会触发此事件，通过此事件可以观察到每个人文件加载成功或失败情况。

* 当前仅文件和图片widget会触发此事件。

|参数|描述|
|----|----|
|[event](#widgetactionevent)|widget事件信息|

## onRecoveryStateChanged

`void onRecoveryStateChanged(boolean isEmpty)`

笔迹回收站空与非空的状态变化

当在擦除模式[InputConfig.erase](#inputconfigerase)擦除笔迹时被擦除的笔迹会移动到回收站导致回收站不为空，会触发此事件。
当反复调用还原笔迹[recover](#recover)导致回收站为空时会触发此事件。
当从擦除模式切换到其他模式或白板翻页后会自动清空回收站，同样有可能触发此事件。

|参数|描述|
|----|----|
|isEmpty|true表示回收站为空，false表示不为空，此时可以通过[recover](#recover)来还原一次擦除操作|

# ScreenshotsCallback

截图完成回调

[screenshots](#screenshots)中使用。

## ScreenshotsCallback.done

`void done(@Nullable Bitmap bitmap)`

截图完成

|参数|描述|
|----|----|
|bitmap|截图得到的位图，如果为null表示截图失败，位图大小等于[WhiteBoardView](#whiteboardview)的像素大小|

# WhiteBoardView

白板的显示控件，用于显示白板内容，当前仅支持同时显示一个白板，如果同时放置了多个白板控件，仅最后一个控件会刷新内容。
布局时此控件的大小最好设定为与白板的虚拟大小[WhiteBoardSize](#whiteboardsize)的宽高比保持一致，否则多余的边缘会留白。

# JoinConfig

加入房间时的参数配置

构造函数
    - `public JoinConfig(@NonNull String appId , @NonNull String roomId , @NonNull String userId , @NonNull String token)`

|参数|类型|可空|描述|
|----|----|----|----|
|appId|String|否|SDK分配的应用id|
|roomId|String|否|白板的房间id，房间通常由服务器创建|
|userId|String|否|用户业务系统中的稳定用户id|
|token|String|否|每次加入房间时生成的标识符，与appId，roomId，userId关联，通常由服务器生成|
|roleId|int|否|角色id，默认为0，通常用来标识此用户身份，方便定制用户权限系统|
|sessionId|String|是|用户会话id，用于唯一标识用户，如果用户业务系统中有与userId对应的临时用户标识符，比如session或token等，此临时id可以在此传递，如果userId相同但是sessionId不同的两个用户加入了白板，可以视为相同用户的多设备加入白板实现，如果留空则白板会自动生成一个|
|nickname|String|是|用户名或昵称，在白板中使用的用户名称|
|avatar|String|是|用户头像地址，在白板中显示的用户头像|

# InputConfig

输入模式配置
此类仅提供静态工厂方法。

## InputConfig.pen

`public static InputConfig pen(@ColorInt int color , float thickness)`

创建一个笔书写输入模式配置

|参数|描述|
|----|----|
|color|笔颜色，支持透明度，适当的透明度可以看作是马克笔实现|
|thickness|笔粗细，必须大于0|

## InputConfig.laserPen

`public static InputConfig laserPen(@NonNull LaserType laserType)`

创建一个激光笔输入模式配置

激光笔是一种瞬时的位置指示型输入模式，指示手指位置的内容。

|参数|描述|
|----|----|
|[laserType](#lasertype)|激光笔类型|

## InputConfig.erase

`public static InputConfig erase(float size)`

创建一个橡皮（擦除）输入模式配置

|参数|描述|
|----|----|
|size|橡皮面积|

## InputConfig.geometry

`public static InputConfig geometry(@NonNull GeometryType geometryType , @ColorInt int color , float thickness)`

创建一个几何图形输入模式配置

|参数|描述|
|----|----|
|[geometryType](#geometrytype)|图形类型|
|color|图形边框的颜色|
|thickness|图形边框粗细|

## InputConfig.select

`public static InputConfig select()`

创建一个选择输入模式配置

此模式可以在白板中框选内容。

# FileConfig

向白板插入文件时描述文件信息的配置

构造函数
    - `public FileConfig(@NonNull File file)`
    - `public FileConfig(@NonNull File file , @Nullable String name)`
    - `public FileConfig(@NonNull File file , @Nullable String name , float left , float top)`
    
|参数|类型|可空|描述|
|----|----|----|----|
|file|File|否|要插入的文件，此文件必须有支持的类型后缀，否则会被系统忽略，支持的类型参考[insertFile](#insertfile)|
|name|String|是|指定文件的实际名称，留空会使用file的名称，此名称不会影响系统对file类型的校验，仅做标识用途，比如file本身是随机串文件名，此处可以赋予它有意义的文件名，此名称会在[ActiveWidgetInfo](#activewidgetinfo)中拿到|
|left|float|否|插入文件时的初始位置的左上角横坐标，默认为0|
|top|float|否|插入文件时的初始位置的左上角纵坐标，默认为0|

# ActiveWidgetInfo

被激活的widget信息

白板中的一切都是widget，包括白板，文件，图片，选择框等等，具体参考[WidgetType](#widgettype)。
当用户操作了一个widget或者在它上面书写时，这个widget会被激活，会触发[onWidgetActive](#onwidgetactive)事件。

|参数|类型|可空|描述|
|----|----|----|----|
|id|String|否|widgetId，此widget的唯一标识符，后续对widget的操作都会用到此id，比如[jumpFilePage](#jumpfilepage)和[deleteFile](#deletefile)|
|type|[WidgetType](#widgettype)|否|指示了此widget的类型|
|userId|String|是|此widget创建者的userId，通常白板页是没有创建者的，由服务器创建|
|name|String|是|widget名称，如果此widget是文件或图片时|
|resourceId|String|是|资源id，sdk内部用于标识实际文件的索引，用户通常无需关心|
|path|String|是|文件路径，如果widget是文件或图片，此为它的本地路径（如果插入的原文件是office文件，则此路径是它转换后的pdf路径，并非原始文件）|
|currentPageNumber|int|否|如果widget是文件时，此为当前文件的页码，从1开始|
|pageCount|int|否|如果widget是文件时，此为文件的总页数（如果原文件是office文件，则此页数是转换成pdf后的实际页数）|

# Room

房间信息

加入白板房间成功后会收到此数据。

|参数|类型|可空|描述|
|----|----|----|----|
|roomId|String|否|房间的id，与[joinRoom](#joinroom)时的roomId一致|
|fileGroupId|String|否|白板中的文件在服务器存储的文件组id，用户无需关心|
|chatRoomId|int|否|房间中的聊天室id，暂不支持|

# RoomMember

房间中的成员信息

|参数|类型|可空|描述|
|----|----|----|----|
|userId|String|否|用户业务系统中的稳定用户id|
|sessionId|String|否|用户会话id，用于唯一标识用户，如果成员在[joinRoom](#joinroom)时未传递此参数，则此参数会由白板自动生成|
|roleId|int|否|角色id，默认为0，通常用来标识此用户身份，方便定制用户权限系统|
|nickname|String|是|用户名或昵称|
|avatar|String|是|用户头像地址|

# WhiteBoardPage

白板页信息

|参数|类型|可空|描述|
|----|----|----|----|
|pageId|String|否|白板页id，每个页面的唯一标识符，后续对白板页的操作会用到，比如[jumpBoardPage](#jumpboardpage)，[deleteBoardPage](#deleteboardpage)等|
|pageNumber|int|否|页面序号，从1开始，标识了此页是白板中的第几页|
|thumbnails|String|否|白板页缩略图url，没有时为空字符串|

# WhiteBoardSize

白板尺寸信息

此信息的数值基于白板内部的虚拟大小和坐标系，并非实际渲染窗口的纹理大小（实际的纹理大小由[WhiteBoardView](#whiteboardview)的像素大小决定）。
以下参数由服务器创建白板房间时指定，通常在一个房间中此信息是固定不变的。

|参数|类型|描述|
|----|----|----|
|maxWidth|int|白板最大宽度|
|maxHeight|int|白板最大高度|
|displayWidth|int|白板显示宽度（可视区的宽度，当前仅支持与maxWidth保持一致，即只能垂直延展）|
|displayHeight|int|白板显示高度（可视区的高度，当此参数小于maxHeight时白板可上下滚动）|

## WhiteBoardSize.ZERO

`public static final WhiteBoardSize ZERO = new WhiteBoardSize(0 , 0 , 0 , 0);`

一个空尺寸，在未加入白板时获取到的值。

# WhiteBoardViewport

白板当前可视区信息

所有数值基于白板内部的虚拟大小和坐标系，并非实际渲染窗口的纹理大小。
当白板页滚动时会刷新此数据。

|参数|类型|描述|
|----|----|----|
|size|[WhiteBoardSize](#whiteboardsize)|白板尺寸|
|offsetX|float|当前白板水平偏移（当前仅支持垂直滚动，所以此值总是0）|
|offsetY|float|当前白板垂直偏移|

## WhiteBoardViewport.IDLE

`public static final WhiteBoardViewport IDLE = new WhiteBoardViewport(WhiteBoardSize.ZERO , 0 , 0);`

一个空闲值，在未加入白板时获取到的值。

# WidgetActionEvent

widget动作事件

描述了对文件或图片的关键操作信息，包括加载情况，由谁插入或删除等。
由[onWidgetActionEvent](#onwidgetactionevent)提供。

|参数|类型|可空|描述|
|----|----|----|----|
|sessionId|String|否|动作发出者的sessionId|
|type|[WidgetType](#widgettype)|否|widget类型|
|action|[WidgetAction](#widgetaction)|否|动作类型|
|name|String|是|widget名称|

# BoardStatus

白板房间状态枚举

## BoardStatus.IDLE

空闲状态，表示没有进入白板

## BoardStatus.LOADING

正在进入白板，即调用[joinRoom](#joinroom)之后到成功或失败之前的状态。

## BoardStatus.SUCCESSFUL

加入白板成功

## BoardStatus.FAILED

加入白板失败

## BoardStatus.RECONNECTING

白板正在重连

# GeometryType

几何图形类型

在[InputConfig.geometry](#inputconfiggeometry)中指定要绘制的几何图形。

|名称|图形|
|----|----|
|RECTANGLE|矩形|
|CIRCLE|圆形|
|LINE|直线|
|ARROW|箭头|

# LaserType

激光笔类型

在[InputConfig.laserPen](#inputconfiglaserpen)中指定激光指示点的样式。

|名称|样式|
|----|----|
|LASER_DOT|圆点|
|LASER_HAND|手形图标|
|LASER_ARROWS_WHITE|白色箭头|
|LASER_ARROWS_BLACK|黑色箭头|

# WidgetType

widget类型，白板中的一切都是widget

|名称|类型|
|----|----|
|BOARD|白板|
|FILE|文件，包括pdf和office|
|IMAGE|图片，jpg和png|
|GEOMETRY|几何图形，由[InputConfig.geometry](#inputconfiggeometry)模式绘制|
|SELECTION|选择框，由[InputConfig.select](#inputconfigselect)模式选中的内容|

# WidgetAction

widget动作类型

在[onWidgetActionEvent](#onwidgetactionevent)中指示widget具体发生的动作事件。

|名称|事件|
|----|----|
|UPLOAD|开始上传/插入新widget|
|DELETE|删除widget|
|SUCCESSFUL|widget加载成功|
|FAILED|widget加载失败|

# WhiteBoardErrorCode

错误码

|名称|值|错误含义|
|----|----|----|
|NETWORK_ERROR|100|网络不可用|
|SERVER_ERROR|101|服务器错误或繁忙|
|APP_ID_NOT_EXIST|200|appId不存在|
|ROOM_ID_NOT_EXIST|201|roomId不存在|
|USER_ID_EMPTY|202|userId为空|
|TOKEN_ERROR|203|token错误|
|CONNECT_ROOM_FAILED|300|连接房间失败|
|PAGE_INFO_TIMEOUT|301|等待页数据下发超时|
|ROOM_DISCONNECT|302|房间连接中断，可能是网络波动，也可能是房间中传输了错误数据导致被服务器切断|
