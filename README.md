# Android Gradle Plugin插件 的所有实现方式
**Gradle插件**可以分为两类：**脚本插件**和**对象插件**。

## 1、脚本插件

> **脚本插件**就是一个普通的gradle构建脚本，例如config.gradle，通过在一个config.gradle脚本中定义一系列的task，另一个构建脚本build.gradle通过apply from:'config.gradle'即可引用这个脚本插件。

 首先在项目根目录下新建一个**config.gradle**文件，在该文件中定义所需的**task**,当然也可以在各自模块下创建，引用时注意路径即可。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2020061416274799.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NjgxNTgw,size_16,color_FFFFFF,t_70)
**config.gradle**的内容如下，这里定义了一个名为**scriptPlugin**的**task**，打印一段文本。

```java
//脚本插件
project.task("scriptPlugin") {
    doLast {
        println("$project.name:this is a scriptPlugin")
    }
}
```
然后在需要引用的module的构建脚本中引用config.gradle,例如在app模块的build.gradle的脚本中引用,由于config.gradle建立在根目录下，与app这个模块平级，所以需要注意路径问题../config.gradle

```java
// 使用脚本插件
apply from:'../config.gradle'
```
就是这么简单，此时运行gradle构建即可执行showConfig这个task，运行这个任务 ，我们可以使用以下命令。

```java
//使用任务全拼
$ gradlew scriptPlugin
//使用简写方式
$ gradlew sP
    
```
输出结果如下
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200614165010353.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NjgxNTgw,size_16,color_FFFFFF,t_70)

## 2、对象插件

  

>   **对象插件**是指实现了org.gradle.api.Plugin接口的类。Plugin接口需要实现void apply(T target)这个方法。该方法中的泛型指的是此Plugin可以应用到的对象，而我们通常是将其应用到Project对象上。

编写对象插件主要有三种方式：

    1、直接在gradle脚本文件中

    2、在buildSrc目录下

    3、在独立的项目下

#### 在gradle脚本文件中
打开app模块下的build.gradle文件，在其中编写一个类实现Plugin接口。

```java
// app模块下的build.gradle文件中定义对象插件
class CustomGradlePlugin implements Plugin<Project>{
    @Override
    void apply(Project target) {
        target.task("showCustomPlugin"){
            doLast {
                println("this is CustomGradlePlugin")
            }
        }
    }
}
```
然后通过插件类名引用它

```java
// app模块下的build.gradle文件中，使用对象插件
apply plugin: CustomGradlePlugin   
```
执行插件中定义的task

```java
D:\AndroidStudioProjects\Android_Gradle\GradlePlugin>gradlew showCustomPlugin

> Task :app:showCustomPlugin
this is CustomGradlePlugin

Deprecated Gradle features were used in this build, making it incompatible with Gradle 7.0.
Use '--warning-mode all' to show the individual deprecation warnings.
See https://docs.gradle.org/6.1.1/userguide/command_line_interface.html#sec:command_line_warnin
gs

BUILD SUCCESSFUL in 8s
1 actionable task: 1 executed
```
或者加一个 -q 参数，省略多与信息

```java
D:\AndroidStudioProjects\Android_Gradle\GradlePlugin>gradlew -q showCustomPlugin
this is CustomGradlePlugin
```

#### 在buildSrc目录下
我们可以将插件写在工程根目录下的buildSrc目录下，这样可以在多个模块之间复用该插件。

虽然**buildSrc是Gradle在项目中配置自定义插件的默认目录**，但它并不是标准的Android工程目录，所以使用这种方式需要我们事先手动创建一个buildSrc目录。目录结构如下： 
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200614171847812.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NjgxNTgw,size_16,color_FFFFFF,t_70)在**buildSrc/src/main/groovy**目录下创建自定义plugin，并在build.gradle中引用groovy插件 

```java
//  buildSrc/build.gradle
apply plugin: 'groovy'
dependencies {
    compile gradleApi()
    compile localGroovy()
}
```
然后编写plugin代码

```java
package com.wings.gradle
import org.gradle.api.Plugin
import org.gradle.api.Project

class CustomBuildSrcPlugin implements Plugin<Project>{
    @Override
    void apply(Project project) {
        project.task('showCustomPluginInBuildSrc') {
            doLast {
                println("InBuildSrc: Module Name is $project.name")
            }
        }
    }
}
```
**由于buildSrc目录是gradle默认的目录之一，该目录下的代码会在构建是自动编译打包，并被添加到buildScript中的classpath下，所以不需要任何额外的配置，就可以直接被其他模块的构建脚本所引用。**

注意这里引用的方式可以是**通过类名引用**，也可以**通过给插件映射一个id，然后通过id引用。**

通过类名引用插件的需要使用全限定名，也就是需要带上包名，或者可以先导入这个插件类，如下

```java
// 在app模块下的build.gradle文件中引用
apply plugin:com.wings.gradle.CustomBuildSrcPlugin
```

或者

```java
// 在app模块下的build.gradle文件中引用
import com.wings.gradle.CustomBuildSrcPlugin
apply plugin: CustomBuildSrcPlugin
```

**通过简单的id的方式，我们可以隐藏类名等细节，使的引用更加容易。映射方式很简单，在buildSrc目录下创建resources/META-INF/gradle-plugins/xxx.properties,这里的xxx也就是所映射的id，这里我们假设取名CustomPlugin。具体结构可参考上文buildSrc目录结构。**

CustomPlugin.properties文件中配置该id所对应的plugin实现类

```java
implementation-class=com.wings.gradle.CustomBuildSrcPlugin
```

此时就可以通过id来引用对于的插件了

```java
//在app模块下的build.gradle文件中引用
apply plugin: 'CustomPlugin'
```

> Tips: 我们可能会遇到'**buildSrc' cannot be used as a project name as it is a reserved name**这个错误,如下图所示。
>  
> 这个错误的原因是因为，我们在 setting.gradle 中配置了 buildSrc，我们把 setting.gradle 中配置的
> buildSrc 删掉就 OK 了。

下面是引用自gradle6.3官方文档的解释
> 
> **buildSrc is now reserved as a project and subproject build name**
> 
> Previously, Gradle did not prevent using the name “buildSrc” for a subproject of a multi-project build or as the name of an included build. Now, this is not allowed. The name “buildSrc” is now reserved for the conventional buildSrc project that builds extra build logic. Typical use of buildSrc is unaffected by this change. You will only be affected if your settings file specifies include("buildSrc") or includeBuild("buildSrc").

**大致意思是**：buildSrc现在被保留作为一个工程或者子工程的名字。以前Gradle没有阻止使用buildSrc作为一个在 多工程构建或者作为一个被包括在构建列表名称的 子工程的名字。现在，这样已经不允许了。现在buildSrc这个名字被保留给传统的构建额外逻辑的buildSrc工程。这次改变没有影响典型的buildSrc的用法，仅仅影响在setting.gradle文件中使用include("buildSrc") or includeBuild("buildSrc")方式构建。

#### 在独立工程下

在buildSrc下创建的plugin只能在该工程下的多个模块之间复用代码。如果想要在多个项目之间复用这个插件，我们就需要在一个单独的工程中编写插件，将编译后的jar包上传maven仓库。

单独工程中定义插件跟在 buildSrc 中是一样的，唯一不同的就是我们需要配置上传，这里我们上传到自己的本地目录’loccal’中，这里我从创建一个名字为**StandardAlonePlugin**，目录结构如下:
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200614180349507.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NjgxNTgw,size_16,color_FFFFFF,t_70)从目录结构来看，和buildSrc目录是一致的。区别在于buildSrc下的代码在构建时会自动编译并被引用。而我们在独立项目中编写的插件如果要能正确的被引用到，需要上传到maven仓库中，然后显式地在需要引用的项目中的buildSrcipt中添加对该构件的依赖。

插件代码

```java
package com.wings.standardaloneplugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class StandardAlonePlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        project.task("showStandardAlonePlugin"){
            doLast {
                println("task in StandardAlonePlugin")
            }
        }
    }
}
```
插件项目构建脚本

```java
apply plugin: 'groovy'
apply plugin: 'maven'
dependencies {
    compile gradleApi()
    compile localGroovy()
}
//以上都为固定写法
//打包到本地或者远程Maven库
uploadArchives {
    repositories {
        mavenDeployer {
            pom.groupId = 'com.wings.plugin'   //groupId
            pom.artifactId = 'standardAlonePlugin'  //artifactId
            pom.version = '1.0.0' //版本号
            //提交到本地目录：
            repository(url: uri('../alonePlugin'))

        }
    }
}
```
这里与buildSrc不同的是，我们引用了apply plugin 'maven'，通过maven插件，我们可以轻松的配置group，version 以及 uploadArchives的相关属性，然后执行./gradlew uploadArchives这个任务，就可以将构件打包后上传到maven仓库了。同样为了示例简单，我们上传到一个本地仓库repository(url: uri('../alonePlugin'))中，还可以上传到JCenter中。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2020061418074496.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NjgxNTgw,size_16,color_FFFFFF,t_70)
上传之后就可以在项目根目录下找到alonePlugin这个目录了。最后我们通过给根目录下的build.gradle配置buildScript的classpath，就可以引用这个插件了。注意，classpath的格式为group:artifact:version

```java
buildscript {
    repositories {
        google()
        jcenter()
        maven{
            url uri('./alonePlugin/')
        }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:4.0.0"
        classpath "com.wings.plugin:standardAlonePlugin:1.0.0"
    }
}
```
引用插件

```java
//在app模块下的build.gradle文件中引用
apply plugin: 'myAlonePlugin'
```

执行StandAlonePlugin中定义的任务

```java
D:\AndroidStudioProjects\Android_Gradle\GradlePlugin>gradlew -q showStandardAlonePlugin
task in StandardAlonePlugin
```
## 总结
Gradle 插件的所有使用方式就介绍完毕了，有兴趣的同学可以亲自动手试试

Gradle插件 https://mp.weixin.qq.com/s/KCpl0CNgwMv0CgvbadNK6A

Gradle学习 http://www.imooc.com/wiki/gradlebase/intro.html

任玉刚写作平台 http://renyugang.io/post/75


