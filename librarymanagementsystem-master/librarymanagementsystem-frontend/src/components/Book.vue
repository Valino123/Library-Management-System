<!-- TODO: YOUR CODE HERE -->
<template>
    <el-scrollbar height="100%" style="width: 100%; height: 100%; ">
        <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold; ">图书管理</div>    
        <!-- <p style="margin-left: 40px; margin-top: 50px;">这里是图书管理页面，请尝试实现。</p> -->
        
        <!-- 任务卡片显示区 -->
        <div style="display: flex;flex-wrap: wrap; justify-content: start;">

            <!-- 任务卡片 -->
            <div class="taskBox" v-for="(task, index) in tasks" v-show="task.name" :key="index" @click="todo(index+1)" style="display: flex; justify-content: center; align-items: center;">

                <!-- 卡片内容 -->
                <div style="text-align: center; font-size: 20px;">
                    <p style="padding: 2.5px;"><span style="font-weight: bold;">{{ task.name }}</span></p>
                    
                </div>

            </div>

        </div>


        <!-- 图书入库对话框 -->
        <el-dialog v-model="todo1Visible" title="图书入库" width="30%" align-center>
            <!-- <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                种类：
                <el-input v-model="todo1Info.category" style="width: 12.5vw;" clearable />
            </div> -->
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">种类：</div>
                <el-input v-model="todo1Info.category" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">标题：</div>
                <el-input v-model="todo1Info.title" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">出版社：</div>
                <el-input v-model="todo1Info.press" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">出版年份：</div>
                <el-input v-model="todo1Info.publishYear" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">作者：</div>
                <el-input v-model="todo1Info.author" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">价格：</div>
                <el-input v-model="todo1Info.price" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">库存：</div>
                <el-input v-model="todo1Info.stock" style="width: 12.5vw;" clearable />
            </div>

            <template #footer>
                <span>
                    <el-button @click="todo1Visible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmTodo1"
                        :disabled="todo1Info.title.length === 0 || todo1Info.author.length === 0
                                || todo1Info.category.length === 0 || todo1Info.press.length === 0
                                || todo1Info.publishYear.length === 0 || todo1Info.stock.length === 0 
                                || todo1Info.price.length === 0">
                        确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 图书批量入库对话框 -->
        <el-dialog v-model="todo2Visible" title="批量入库" width="30%" align-center>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">url：</div>
                <el-input v-model="todo2Info.url" style="width: 12.5vw;" clearable />
            </div>


            <template #footer>
                <span>
                    <el-button @click="todo2Visible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmTodo2"
                    :disabled="todo2Info.url.length === 0">
                    确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 增加库存对话框 -->
        <el-dialog v-model="todo3Visible" title="批量入库" width="30%" align-center>
            <!-- <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                种类：
                <el-input v-model="todo1Info.category" style="width: 12.5vw;" clearable />
            </div> -->
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">图书序号：</div>
                <el-input v-model="todo3Info.bookId" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">增加库存：</div>
                <el-input v-model="todo3Info.deltaStock" style="width: 12.5vw;" clearable />
            </div>


            <template #footer>
                <span>
                    <el-button @click="todo3Visible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmTodo3"
                    :disabled="todo3Info.bookId.length === 0 || todo3Info.deltaStock.length === 0">
                    确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 借阅图书对话框 -->
        <el-dialog v-model="todo4Visible" title="借阅图书" width="30%" align-center>
            <!-- <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                种类：
                <el-input v-model="todo1Info.category" style="width: 12.5vw;" clearable />
            </div> -->
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">图书序号：</div>
                <el-input v-model="todo4Info.bookId" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">借书证序号：</div>
                <el-input v-model="todo4Info.cardId" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">借书时间：</div>
                <el-input v-model="todo4Info.borrowTime" style="width: 12.5vw;" clearable />
            </div>


            <template #footer>
                <span>
                    <el-button @click="todo4Visible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmTodo4"
                    :disabled="todo4Info.bookId.length === 0 || todo4Info.cardId.length === 0 || todo4Info.borrowTime.length ===0">
                    确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 归还图书对话框 -->
        <el-dialog v-model="todo5Visible" title="归还图书" width="30%" align-center>
            <!-- <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                种类：
                <el-input v-model="todo1Info.category" style="width: 12.5vw;" clearable />
            </div> -->
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">图书序号：</div>
                <el-input v-model="todo5Info.bookId" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">借书证序号：</div>
                <el-input v-model="todo5Info.cardId" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">借书时间：</div>
                <el-input v-model="todo5Info.borrowTime" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">还书时间：</div>
                <el-input v-model="todo5Info.returnTime" style="width: 12.5vw;" clearable />
            </div>

            <template #footer>
                <span>
                    <el-button @click="todo5Visible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmTodo5"
                    :disabled="todo5Info.bookId.length === 0 || todo5Info.cardId.length === 0 || 
                                todo5Info.borrowTime.length ===0 || todo5Info.returnTime.length ===0">
                    确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 修改图书信息对话框 -->
        <el-dialog v-model="todo6Visible" title="修改图书信息" width="30%" align-center>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">图书序号：</div>
                <el-input v-model="todo6Info.bookId" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">种类：</div>
                <el-input v-model="todo6Info.category" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">标题：</div>
                <el-input v-model="todo6Info.title" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">出版社：</div>
                <el-input v-model="todo6Info.press" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">出版年份：</div>
                <el-input v-model="todo6Info.publishYear" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">作者：</div>
                <el-input v-model="todo6Info.author" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">价格：</div>
                <el-input v-model="todo6Info.price" style="width: 12.5vw;" clearable />
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px;">
                <div style="font-weight: bold;">库存：</div>
                <el-input v-model="todo6Info.stock" style="width: 12.5vw;" clearable />
            </div>

            <template #footer>
                <span>
                    <el-button @click="todo6Visible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmTodo6"
                        :disabled="todo6Info.title.length === 0 || todo6Info.author.length === 0
                                || todo6Info.category.length === 0 || todo6Info.press.length === 0
                                || todo6Info.publishYear.length === 0 || todo6Info.stock.length === 0 
                                || todo6Info.price.length === 0 || todo6Info.bookId.length === 0" >
                        确定</el-button>
                </span>
            </template>
        </el-dialog>

    </el-scrollbar>
</template>

<script>
import { ElMessage } from 'element-plus';
import axios from 'axios'
import { Delete, Edit, Search } from '@element-plus/icons-vue'


export default{
    data(){
        return {
            tasks:[{
                name: '图书入库'
            },{
                name: '批量入库'
            },{
                name: '增加库存'
            },{
                name: '借阅图书'
            },{
                name: '归还图书'
            },{
                name: '修改图书信息'
            },{
                name: '图书查询'
            }],
            todo1Visible: false,
            todo2Visible: false,
            todo3Visible: false,
            todo4Visible: false,
            todo5Visible: false,
            todo6Visible: false,
            todo7Visible: false,

            todo1Info:{
                category: '',
                title:'',
                press:'',
                publishYear:'',
                author:'',
                price:'',
                stock:''

            },
            todo2Info:{
                url:''
            },
            todo3Info:{
                bookId:'',
                deltaStock:''
            },
            todo4Info:{
                cardId:'',
                bookId:'',
                borrowTime:''
            },
            todo5Info:{
                cardId:'',
                bookId:'',
                borrowTime:'',
                returnTime:''
            },
            todo6Info:{
                bookId:'',
                category:'',
                title:'',
                press:'',
                publishYear:'',
                author:'',
                price:'',
                stock:''
            },
            todo7Info:{

            },
        }
    },
    methods:{
        todo(index){
            if(index == 1){
                this.todo1Visible = true;
                // this.todo1();
            }else if(index == 2){
                this.todo2Visible = true;
                // this.todo2();
            }else if(index == 3){
                this.todo3Visible = true;
                // this.todo3();
            }else if(index == 4){
                this.todo4Visible = true;
                // this.todo4();
            }else if(index == 5){
                this.todo5Visible = true;
                // this.todo5();
            }else if(index == 6){
                this.todo6Visible = true;
                // this.todo6();
            }else{
                this.todo7Visible = true;
                this.$router.push('/lookup-book');
                // this.todo7();
            }
        },
        ConfirmTodo1(){//图书入库
            axios.post("/book",
                {//request body
                    task: "图书入库",
                    category: this.todo1Info.category,
                    title: this.todo1Info.title,
                    press: this.todo1Info.press,
                    publishYear: this.todo1Info.publishYear,
                    author: this.todo1Info.author,
                    price: this.todo1Info.price,
                    stock: this.todo1Info.stock
                })
                .then(response => {
                    this.todo1Visible = false;
                    if(response.data){
                        ElMessage.success("图书入库成功");
                    }else{
                        ElMessage.error("图书入库失败");
                    }
                    this.todo1Info={
                        category: '',
                        title:'',
                        press:'',
                        publishYear:'',
                        author:'',
                        price:'',
                        stock:''
                    };
                })
        },
        ConfirmTodo2(){// 批量入库
            axios.post("/book",
                {//request body
                    task: "批量入库",
                    url: this.todo2Info.url
                })
                .then(response => {
                    this.todo2Visible = false;
                    if(response.data){
                        ElMessage.success("批量入库成功");
                    }else{
                        ElMessage.error("批量入库失败");
                    }
                    this.todo2Info={
                        url:''
                    };
                })
        },  
        ConfirmTodo3(){// 增加库存
            axios.post("/book",
                {//request body
                    task: "增加库存",
                    bookId: this.todo3Info.bookId,
                    deltaStock: this.todo3Info.deltaStock
                })
                .then(response => {
                    this.todo3Visible = false;
                    if(response.data){
                        ElMessage.success("增加库存成功");
                    }else{
                        ElMessage.error("增加库存失败");
                    }
                    this.todo3Info={
                        bookId:'',
                        deltaStock:''
                    };
                })
        },
        ConfirmTodo4(){// 借阅图书
            axios.post("/book",
                {//request body
                    task: "借阅图书",
                    bookId: this.todo4Info.bookId,
                    cardId: this.todo4Info.cardId,
                    borrowTime: this.todo4Info.borrowTime
                })
                .then(response => {
                    this.todo4Visible = false;
                    if(response.data){
                        ElMessage.success("借阅图书成功");
                    }else{
                        ElMessage.error("借阅图书失败");
                    }
                    this.todo4Info={
                        cardId:'',
                        bookId:'',
                        borrowTime:''
                    };
                })
        },
        ConfirmTodo5(){//归还图书
            axios.put("/book",
                {//request body
                    task: "归还图书",
                    bookId: this.todo5Info.bookId,
                    cardId: this.todo5Info.cardId,
                    borrowTime: this.todo5Info.borrowTime,
                    returnTime: this.todo5Info.returnTime
                })
                .then(response => {
                    this.todo5Visible = false;
                    if(response.data){
                        ElMessage.success("归还图书成功");
                    }else{
                        ElMessage.error("归还图书失败");
                    }
                    this.todo5Info={
                        cardId:'',
                        bookId:'',
                        borrowTime:'',
                        returnTime:''
                    };
                })
        },
        ConfirmTodo6(){//修改图书信息
            axios.put("/book",
                {//request body
                    task: "修改图书信息",
                    bookId : this.todo6Info.bookId,
                    category: this.todo6Info.category,
                    title: this.todo6Info.title,
                    press: this.todo6Info.press,
                    publishYear: this.todo6Info.publishYear,
                    author: this.todo6Info.author,
                    price: this.todo6Info.price,
                    stock: this.todo6Info.stock
                })
                .then(response => {
                    this.todo6Visible = false;
                    if(response.data){
                        ElMessage.success("图书信息修改成功");
                    }else{
                        ElMessage.error("图书信息修改失败");
                    }
                    this.todo6Info= {
                        bookId:'',
                        category:'',
                        title:'',
                        press:'',
                        publishYear:'',
                        author:'',
                        price:'',
                        stock:''
                    };
                    
                })
        },
        ConfirmTodo7(){//图书查询

        }

    },
    mounted(){

    }
}
</script>


<style scoped>
.taskBox {
    cursor: pointer;
    height: 250px;
    width: 200px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    text-align: center;
    margin-top: 40px;
    margin-left: 27.5px;
    margin-right: 10px;
    padding: 7.5px;
    padding-right: 10px;
    padding-top: 15px;
}
</style>

<!-- 
图书入库
|__ 单本
    |__ 对话框->输入各个信息
|__ 批量
    |__ 对话框->输入路径
增加库存
|__ 对话框->输入ID->查询显示->stock
修改图书信息
|__ 对话框->输入ID->查询显示->更改信息
借书
还书
图书查询
 -->