# MybatisPlus作业

### 1.  代码：根据需求，通过mybatisplus实现功能

```
CREATE TABLE student (
     id INT, -- 编号
     name VARCHAR(20), -- 姓名
     age INT, -- 年龄
     sex VARCHAR(5), -- 性别
     address VARCHAR(100), -- 地址
     math INT, -- 数学
     english INT -- 英语
);

INSERT INTO 
	student (id,name,age,sex,address,math,english) 
VALUES 
	(1,'马云',55,'男','杭州',66,78),
	(2,'马化腾',45,'女','深圳',98,87),
	(3,'马景涛',55,'男','香港',56,77),
	(4,'柳岩',20,'女','湖南',76,65),
	(5,'柳青',20,'男','湖南',86,NULL),
	(6,'刘德华',57,'男','香港',99,99),
	(7,'马德',22,'女','香港',99,99),
	(8,'德玛西亚',18,'男','南京',56,65);

-- 查询 math 分数大于 80 分的学生
-- 查询 english 分数小于或等于 80 分的学生
-- 查询 age 等于 20 岁的学生
-- 查询 age 不等于 20 岁的学生
-- 查询 age 大于 35 且性别为男的学生(两个条件同时满足)
-- 查询 age 大于 35 或性别为男的学生(两个条件其中一个满足)
-- 查询 id 是 1 或 3 或 5 的学生
-- 查询 english 成绩大于等于 75，且小于等于 90 的学生
-- 查询姓马的学生
-- 查询姓名中包含'德'字的学生
```



- 代码：

```java
@SpringBootTest
@Slf4j
class Mp04ApplicationTests {
    @Autowired
    private StudentMapper studentMapper;

    /**
     * -- 查询 math 分数大于 80 分的学生
     */
    @Test
    void test80(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.gt(Student::getMath,80);
        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("Students:{}", students);
    }

    /**
     * -- 查询 english 分数小于或等于 80 分的学生
     */
    @Test
    public void testEnglish80(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(Student::getEnglish,80);

        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("Students:{}", students);
    }

    /**
     * -- 查询 age 等于 20 岁的学生
     */
    @Test
    public void testAge20(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getAge,20);

        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("Students:{}", students);
    }

    /**
     * -- 查询 age 不等于 20 岁的学生
     */
    @Test
    public void testAgeNotEq20(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(Student::getAge,20);

        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("Students:{}", students);
    }

    /**
     * -- 查询 age 大于 35 且性别为男的学生(两个条件同时满足)
     */
    @Test
    public void age35AndGender(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(Student::getAge,35).and(studentLambdaQueryWrapper ->{
            studentLambdaQueryWrapper.eq(Student::getSex,"男");
        });

        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("Students:{}", students);
    }

    // -- 查询 age 大于 35 或性别为男的学生(两个条件其中一个满足)
    @Test
    public void test1(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(Student::getAge,35).or(studentLambdaQueryWrapper ->{
            studentLambdaQueryWrapper.eq(Student::getSex,"男");
        });

        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("Students:{}", students);
    }

    //-- 查询 id 是 1 或 3 或 5 的学生
    @Test
    public void test2(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        List<Integer> ids = new ArrayList<>();
        Collections.addAll(ids,1,3,5);
        queryWrapper.in(Student::getId,ids);
        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("Students:{}", students);
    }

    //-- 查询 english 成绩大于等于 75，且小于等于 90 的学生
    @Test
    public void test3(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Student::getEnglish,75,90);
        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("students:{}",students);
    }

    //-- 查询姓马的学生
    @Test
    public void test4(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(Student::getName,"马");
        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("students:{}",students);
    }

    //-- 查询姓名中包含'德'字的学生
    @Test
    public void test5(){
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Student::getName,"德");
        List<Student> students = studentMapper.selectList(queryWrapper);
        log.info("students:{}",students);
    }
}
```



### 2. 问答：今天学习了Mybatisplus的哪些注解，分别有什么作用？

```java
  @TableName("xxxx") //表名与实体类名映射
  @TableField(exist) //告诉框架表中没有这个字段
  @TableId //主键相关
  @TableLogic // 逻辑键
  @Verison // 乐观锁版本控制
```
