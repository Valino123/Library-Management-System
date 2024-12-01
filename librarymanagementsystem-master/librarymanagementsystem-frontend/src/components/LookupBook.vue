<template>
    <el-scrollbar height="100%" style="width: 100%;">
        <div>
            <!-- Title -->
            <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold;">
                图书查询
            </div>
            <div style="margin-top: 20px; margin-left: 40px; font-size: 1.5em; font-weight: bold;">
                筛选条件
            </div>
            <!-- Filters -->
            <div class="filter" v-for="(value,key) in filters" :key="key">
                <div class="filter-name">{{fieldNames[key]}}</div>
                <el-input v-model="filters[key]" class="filter-input" clearable></el-input>
            </div>
            <div style="margin-top: 20px; margin-left: 40px; font-size: 1.5em; font-weight: bold;">
                排序参照
            </div>
            <!-- Selector -->
            <div class="selector">
                <el-select v-model="selected" placeholder="Select">
                    <el-option
                        v-for="item in options"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </div>
            <div style="margin-top: 20px; margin-left: 40px; font-size: 1.5em; font-weight: bold;">
                顺序
            </div>
            <div class="selector">
                <el-select v-model="order" placeholder="Select">
                    <el-option
                        v-for="item in orderOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </div>
            <!-- Search Button -->
            <el-button type="primary" @click="searchBooks">查询</el-button>
            <!-- Results Table -->
            <el-table v-if="books.length>0" :data="books" 
                style="width: 100%; margin-left: 50px; margin-top: 30px; margin-right: 50px; max-width: 80vw;">
                <el-table-column prop="bookId" label="bookId"></el-table-column>
                <el-table-column prop="category" label="category"></el-table-column>
                <el-table-column prop="title" label="title"></el-table-column>
                <el-table-column prop="press" label="press"></el-table-column>
                <el-table-column prop="publishYear" label="publishYear"></el-table-column>
                <el-table-column prop="author" label="author"></el-table-column>
                <el-table-column prop="price" label="price"></el-table-column>
                <el-table-column prop="stock" label="stock"></el-table-column>
            </el-table>
        </div>
        
            
            
    </el-scrollbar>
</template>

<script>
import axios from 'axios';

export default {
    data() {
        return {
            fieldNames: {
                category: '种类',
                title: '标题',
                press: '出版社',
                minPublishYear: '最小出版年份',
                maxPublishYear: '最大出版年份',
                author: '作者',
                minPrice: '最低价格',
                maxPrice: '最高价格'
            },
            filters:{
                category:'',
                title:'',
                press:'',
                minPublishYear:'',
                maxPublishYear:'',
                author:'',
                minPrice:'',
                maxPrice:''
            },
            isShow: false,
            books:[],
            selected:'',
            options:[
                { value: 'category', label: '种类' },
                { value: 'title', label: '标题' },
                { value: 'author', label: '作者' },
                { value: 'book_id', label: '图书序号' },
                { value: 'press', label: '出版社' },
                { value: 'price', label: '价格' },
                { value: 'astock', label: '存量' },
                { value: 'publish_year', label: '出版年份' }
            ],
            order:'',
            orderOptions:[
                {value: 'asc', label: '升序'},
                {value: 'desc', label: '降序'}
            ]
        };
    },
    
    methods: {
      
        async searchBooks(){
            this.books = []
            const response = await axios.get('/book', {
                params: {
                    filters: JSON.stringify(this.filters),
                    selected: this.selected,
                    order: this.order
                }});
            this.books = response.data;
            this.isShow = true
            this.filters={
                category:'',
                title:'',
                press:'',
                minPublishYear:'',
                maxPublishYear:'',
                author:'',
                minPrice:'',
                maxPrice:''
            }
        }
    }
}
</script>

<style scoped>
.filter{
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    margin-top: 20px;
    margin-left: 150px;
}

.filter-name{
    font-weight: bold;
    text-align: left;
}
.filter-input{
    text-align: left;
    width: 200px;
    margin-right: 200px;
}
.selector{
    margin-top: 20px;
    margin-left: 150px;
}

</style>