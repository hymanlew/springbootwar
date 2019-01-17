package com.hyman.springbootwar.dao;

import com.hyman.springbootwar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * 在 spring 中使用了 JdbcTemplate访问数据库，介绍了一种基本的数据访问方式。
 * 然而在实际开发过程中，对数据库的操作无非就“增删改查”。就最为普遍的单表操作而言，除了表和字段不同外，语句都是类似的，开发人
 * 员需要写大量类似而枯燥的语句来完成业务逻辑。
 *
 * 为了解决这些大量枯燥的数据操作语句，我们第一个想到的是使用 ORM 框架，比如：Hibernate。通过整合 Hibernate 之后，我们以操作
 * Java实体的方式最终将数据改变映射到数据库表中。
 *
 * 为了解决抽象各个Java实体基本的“增删改查”操作，我们通常会以泛型的方式封装一个模板 Dao 来进行抽象简化，但是这样依然不是很方
 * 便，我们需要针对每个实体编写一个继承自泛型模板 Dao 的接口，再编写该接口的实现。虽然一些基础的数据访问已经可以得到很好的复
 * 用，但是在代码结构上针对每个实体都会有一堆 Dao 的接口和实现。
 *
 * 由于模板Dao的实现，使得这些具体实体的Dao层已经变的非常“薄”，有一些具体实体的Dao实现可能完全就是对模板Dao的简单代理，并且往
 * 往这样的实现类可能会出现在很多实体上。
 *
 * Spring-data-jpa的出现正可以让这样一个已经很“薄”的数据访问层变成只是一层接口的编写方式。
 * 我们只需要通过编写一个继承自 JpaRepository的接口就能完成数据访问，Spring-data-jpa 依赖于 Hibernate。但是使用 JpaRepository
 * 的 save 方法，其源码是设置主键 id 从头开始插入，即从 1 开始，所以会连续报错（不能执行语句，唯一约束异常），直到连续执行语句
 * id 自增到与主键不冲突时才能正常插入数据。
 *
 * 所以不能使用  JpaRepository，而是使用其父接口 CrudRepository，其save方法是相当于 merge+save ，它会先判断记录是否存在，如果
 * 存在则更新，不存在则插入记录（与实体类中主键的生成策略有关）。
 */

//JpaRepository<实体类名，实体类主键类型>
//public interface UserRepository extends CrudRepository<User,Integer> {
public interface UserRepository extends JpaRepository<User,Integer> {

    /**
     * 可以看到我们这里没有任何类SQL语句就完成了两个条件查询方法。这就是Spring-data-jpa的一大特性：通过解析方法名创建查询。
     *
     * 除了通过解析方法名来创建查询外，它也提供通过使用@Query 注解来创建查询，您只需要编写JPQL语句，并通过类似 “:name” 来映射
     * @Param 指定的参数，就像例子中的第三个findUser函数一样。
     *
     */
    User findByName(String name);

    User findByNameAndAge(String name, Integer age);

    @Query("select id,name from User u where u.name=:name")
    User findUser(@Param("name") String name);

    /**
     * 在 Spring-data-jpa 中，只需要编写类似上面这样的接口就可实现数据访问。不再像我们以往编写了接口时候还需要自己编写接口实现
     * 类，直接减少了我们的文件清单。
     *
     * 该接口继承自 JpaRepository，通过查看 JpaRepository 接口的API文档，可以看到该接口本身已经实现了创建（save）、更新（save）
     * 、删除（delete）、查询（findAll、findOne）等基本操作的函数，因此对于这些基础操作的数据访问就不需要开发者再自己定义。
     *
     * 在我们实际开发中，JpaRepository接口定义的接口往往还不够或者性能不够优化，我们需要进一步实现更复杂一些的查询或操作。
     */
}
