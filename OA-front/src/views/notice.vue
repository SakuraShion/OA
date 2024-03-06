<template>
	<div class="app-container">

		<el-row :gutter="10" class="mb8">
			<el-col :span="1.5">
				<el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple"
					@click="handleDelete">删除</el-button>
			</el-col>
		</el-row>

		<el-table @selection-change="handleSelectionChange" v-loading="dataListLoading" :data="dataList"
			style="width: 100%;">
			<el-table-column type="selection" width="55" align="center" />
			<el-table-column type="index" header-align="center" align="center" width="100" label="序号">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column label="操作状态" align="center" prop="senderName">
				<template #default="scope">
					<el-tag :type="scope.row.senderName === '系统消息'?'warning':'success'">{{ scope.row.senderName}}
					</el-tag>
				</template>
			</el-table-column>
			<el-table-column label="创建时间" align="center" prop="sendTime" />
			<el-table-column label="消息创建人" align="center" prop="senderId" />
			<el-table-column label="操作状态" align="center" prop="readFlag">
				<template #default="scope">
					<el-tag :type="scope.row.readFlag === true?'':'danger'">
						{{ scope.row.readFlag === true ? "已读" : "未读" }}</el-tag>
				</template>
			</el-table-column>
			<el-table-column label="操作" align="center" class-name="small-padding fixed-width">
				<template #default="scope">
					<el-button size="mini" type="text" icon="el-icon-view" 
						@click="queryNotice(scope.row,scope.index)">查看
					</el-button>
				</template>
			</el-table-column>
		</el-table>

		<el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" :current-page="pageIndex"
			:page-sizes="[10, 20, 50]" :page-size="pageSize" :total="totalCount"
			layout="total, sizes, prev, pager, next, jumper"></el-pagination>
			
		<notice-info v-if="open" ref="noticeInfo"></notice-info>
	</div>
</template>

<script>
import NoticeInfo from './notice-info.vue';
export default {
	name: "Notice",
	components: { NoticeInfo },
	inject: ['queryMessage'],
	data() {
		return {
			// 遮罩层
			dataListLoading: false,
			// 总条数
			totalCount: 0,
			// 表格数据
			dataList: [],
			pageIndex: 1,
			pageSize: 10,
			// 选中数组
			ids: [],
			// 非多个禁用
			multiple: true,
			// 是否显示弹出层
			open: false,
		}
	},

	created() {
		this.loadDataList();
	},

	methods: {
		loadDataList: function() {
			let that = this;
			that.dataListLoading = true;

			let data = {
				page: that.pageIndex,
				length: that.pageSize
			};

			that.$http('message/searchMessageByPage', 'POST', data, true, function(resp) {
				let page = resp.page;
				let list = page.list;

				that.dataList = list;
				that.totalCount = page.totalCount;
				that.dataListLoading = false;
			});

		},
		
		queryNotice: function(row) {
			// 为 true 时显示消息详情控件，false 隐藏
			this.open = true;
			this.$nextTick(() => {
				this.$refs.noticeInfo.init(row);
			})
			
			let that = this;
			
			if(row.readFlag == false) {
				that.$http('message/updateUnreadMessage', 'POST', {id: row.refId}, true, function(resp) {
					if(resp.rows) {
						that.queryMessage();
						row.readFlag = true;
					}
				});
			}

		},

		// 当分页的页大小发生改变时调用此函数
		sizeChangeHandle(val) {
			this.pageSize = val;
			// 当页大小发生改变，就返回第一页
			this.pageIndex = 1;
			this.loadDataList();
		},

		currentChangeHandle(val) {
			this.pageIndex = val;
			this.loadDataList();
		},

		/** 多选框选中数据 */
		handleSelectionChange(selection) {
			this.ids = selection.map(item => item.refId);
			console.log(this.ids)
			this.multiple = !selection.length;
		},

		/** 详细按钮操作 */
		handleView(row) {
			this.updateMsgNum();
			this.open = true;
			this.form = row;
		},
		/** 删除按钮操作 */
		handleDelete: function() {
			let that = this;
			let ids = this.ids;
			
			console.log(this.ids);
				
			if(ids == 0) {
				that.$message({
					message: '没有选中记录',
					type: 'error',
					duration: 1200
				});
			}
			else {
				that.$confirm(`确定删除选中记录?`,  '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					that.$http('message/deleteMessageRefById', 'POST', {ids: ids}, true, function(resp) {
						if(resp.rows > 0) {
							that.$message({
								message: '操作成功',
								type: 'success',
								duration: 1200
							});
							
							that.loadDataList();
						}
						else {
							that.$message({
								message: '删除记录失败',
								type: 'warning',
								duration: 1200
							});
						}
					})
				})
			}
		},
	}
};
</script>
