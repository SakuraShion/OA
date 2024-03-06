<template>
  <div v-if="isAuth(['ROOT'])">
    <el-form :model="dataForm" ref="dataForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="登录地址" prop="ipaddr">
        <el-input
          v-model="dataForm.ipaddr"
          placeholder="请输入登录地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户名称" prop="userName">
        <el-input
          v-model="dataForm.userName"
          placeholder="请输入用户名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>

    </el-form>
    <el-table
      v-loading="dataListLoading"
      :data="dataList"
      style="width: 100%;"
    >
      <el-table-column label="序号" type="index" align="center">
        <template slot-scope="scope">
          <span>{{(pageIndex - 1) * pageSize + scope.$index + 1}}</span>
        </template>
      </el-table-column>
      <el-table-column label="会话编号" align="center" prop="tokenId" :show-overflow-tooltip="true" />
      <el-table-column label="登录名称" align="center" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column label="部门名称" align="center" prop="deptName" />
      <el-table-column label="主机" align="center" prop="ipaddr" :show-overflow-tooltip="true" />
      <el-table-column label="登录地点" align="center" prop="loginLocation" :show-overflow-tooltip="true" />
      <el-table-column label="浏览器" align="center" prop="browser" />
      <el-table-column label="操作系统" align="center" prop="os" />
      <el-table-column label="登录时间" align="center" prop="loginTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
			v-if="isAuth(['ROOT'])"
			@click="handleForceLogout(scope.row)"
          >强退</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="pageIndex" :limit.sync="pageSize" />
  </div>
</template>

<script>

export default {
  name: "Online",
  data() {
    return {
	
		// 遮罩层
		dataListLoading: false,
		// 总条数
		total: 0,
		// 表格数据
		dataList: [],
		pageIndex: 1,
		pageSize: 10,
		// 查询参数
		dataForm: {
			ipaddr: '',
			userName: '',
		},
	};
  },
	created() {
		// this.loadDataList();
	},
	methods: {
		/** 查询登录日志列表 */
		loadDataList: function() {
			let that = this;
			that.dataListLoading = true;
			
			that.$http('user/searOnline', 'POST', data, true, function(resp) {
				let page = resp.page;
				let list = page.list;
				
				for(let one of list) {
					if(one.status == 1) {
						one.status = '在职';
					}
					else {
						one.status = '离职'
					}
				}
				
				that.dataList = list;
				that.totalCount = page.totalCount;
				that.dataListLoading = false;
			});
		},
		/** 搜索按钮操作 */
		handleQuery() {
		  this.pageIndex = 1;
		  this.loadDataList();
		},
		/** 重置按钮操作 */
		resetQuery() {
		  this.dataForm.ipaddr = '';
		  this.dataForm.userName = '';
		  this.handleQuery();
		},
		/** 强退按钮操作 */
		handleForceLogout(row) {
			this.$confirm('是否确认强退名称为"' + row.userName + '"的用户？', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(function() {
				return forceLogout(row.tokenId);
			}).then(() => {
				this.loadDataList();
				this.$modal.msgSuccess("强退成功");
			}).catch(() => {});
		}
	}
};
</script>
