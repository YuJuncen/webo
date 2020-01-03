# WebO
这是个相当简单的社交网络后端。

它只能发送文本、转发、评论，修改一些个人信息、从 [gravatar](https://gravatar.com) 获取头像。

仅此而已。但是它确实是我们移动开发大作业的后端。

## 部署
您可以使用最简单的方式来运行这个程序：
```bash
./gradlew bootRun
```
默认情况下，这个程序会在 `h2 database` 中运行（您可以在 http://localhost:8080/h2 中管理这个数据库！），
但是这个数据库在内存中，因此每次关机都会丢失数据。

如果需要配合其他数据库使用，您应该在 `app.properites` 中修改 `DataResource` 相关的配置，并且引入您需要的数据库的相关驱动。

> ⚠️警告！
>
> 因为作者偷懒，在上传的时候忘了删除 `jwtSecret`，所以安全起见，在您运行这个的时候，请修改它（也在 `app.properties` 中）！

---
> *以下是交作业的时候写的一些文档。*
## 数据库、ORM

这部分我们使用了 `Spring Data JPA` 框架完成。

这个框架看起来和 `MyBatis` 一类的并不尽然相同，和接近 `SQL` 的后者相比，前者更加接近 `Java`——整个过程中，我们无需写任何 `SQL` 或者 DDL，仅仅需要在 Java 的类上加上注解：

```kotlin
@Entity
@Table(uniqueConstraints = [UniqueConstraint(name="username-uniq", columnNames = ["username"])],
        indexes = [Index(name="username-index", columnList = "username", unique = true)])
data class User(
        @Id
        @GeneratedValue
        var id: Int?,

        @NotNull
        @Column(unique = true, name = "username")
        var username : String,

        @NotNull
        private var password : String,

        @NotNull
        var nickname : String = username,

        @NotNull
        @ElementCollection
        var following : MutableSet<Int> = mutableSetOf(),

        @NotNull
        @ElementCollection
        var followedBy : MutableSet<Int> = mutableSetOf(),

        var email : String? = null,

        var bio: String? = null
)
```

限制、索引等等都可以在注解中写出来。

查询就和架构图中一样，依靠于 `Repository` 类，这个类是接口，`Spring data` 会帮助我们用 `hibernate` 去实现它，此时有一点非常特别，我们写的并不是 `SQL`，而是方法名。

`Spring data` 会基于方法名生成 `HQL`，后者是 `hibernate` 的查询语句，来帮助我们从数据库中取出来我们所需，当然，我们也可以为复杂的查询自己指定 `HQL`，就像这样：

```kotlin
interface WeboRepository : PagingAndSortingRepository<Webo, UUID> {
  // 依照方法名字
    fun getFirst10ByPublishedByIsInAndPublishTimeLessThanOrderByPublishTimeDesc(publishedBy: MutableCollection<Int>, publishTime: Instant) : List<Webo>
    fun getFirst10ByPublishTimeLessThanOrderByPublishTimeDesc(publishTime: Instant) : List<Webo>
    fun getFirst10ByPublishedByAndPublishTimeIsBeforeOrderByPublishTimeDesc(publishedBy: Int, publishTime: Instant) : List<Webo>
    fun countWebosByPublishedBy(publishedBy: Int) : Int
  
  // 手动指定 HQL
    @Query( """from Webo as webo
            where webo.publishTime < :publishTime 
                and count(webo.likedBy) > :likes
            order by count(webo.likedBy)""",
            countQuery = """
                select count(id) from Webo 
                where webo.publishTime < :publishTime 
                    and count(webo.likedBy) > likes
            """)
    fun getTop10PublishTimeIsBeforeAndLikesIsMoreThanOrderByLikes(publishTime: Instant, likes: Int)
}
```

### 数据字典

这部分描绘向外部暴露的数据。

#### 用户

这个是用户基本的外部视图：

```kotlin
data class UserNameView(
        val id: Int,
        val username: String,
        val nickname: String,
        val email: String?,
        val bio: String?
)
```

这个是作为 WebO 的用户，所补充的用户视图：

```kotlin
data class WeboUserView(
        val personal: UserNameView,
        val followingCount: Int,
        val followedByCount: Int,
        val weboCount: Int,
        val commentCount: Int
)
```

#### Webo

这个是评论的视图：

```kotlin
data class CommentView(
        val id: UUID? = null,
        val publisher : UserNameView?,
        val content : String,
        val publishTime : Instant,
        val replyTo : UUID? = null
)
```

这个是 Webo 列表中，Webo 的简略视图：

```kotlin
data class WeboView(
        val id: UUID,
        val publishTime: Instant,
        val publishedBy: UserNameView?,
        val message: String,
        val likes: Int,
        val forwards: Int,
        val comments: Int,
        val myselfIsLike: Boolean,
        val forwarding: WeboView?
)
```

## 领域服务

这部分是应用向外界暴露的“服务”，是基于业务逻辑构建的服务。

我们使用 `Spring` 的 `@Service` 注解来将服务注入给 `Controllers` 使用。

例如，和 Webo 相关的提供的“服务”列表如下：

| 名字              | 参数                       | 描述                          |
| ----------------- | -------------------------- | ----------------------------- |
| getFeedFor        | 用户ID                     | 获得某个用户的推荐列表。      |
| getFeed           | 无                         | 获得普遍的推荐列表。          |
| getWebosOf        | 用户ID                     | 获得某个用户发送的 Webo。     |
| getFollowingWebos | 用户ID                     | 获得某个用户关注用户的 Webo。 |
| getWeboById       | Webo ID                    | 获得某个 Webo 的详细信息。    |
| like              | Webo ID，用户 ID           | 让某个用户喜欢某个 Webo。     |
| publishWebo       | 内容，用户 ID              | 发布一篇新的 Webo。           |
| deleteWebo        | Webo ID，用户 ID           | 用某个用户尝试删除某个 Webo。 |
| forward           | Webo ID，用户 ID，转发内容 | 让某个用户转发某个 Webo。     |
| getWeboUserInfo   | 用户 ID                    | 获得某个用户的详细信息。      |

和用户相关的服务则在 `WeboUserService` 和 `UserService` 文件中，此处不加以赘述。

## REST 接口

这里使用了 `Spring MVC`，但是并没有真正的渲染页面，仅仅只是使用了其中的 `@RestController` 部分。

文件中附带了一个接口文档，在那里可以查看相关内容。
