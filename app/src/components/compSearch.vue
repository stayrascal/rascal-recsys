<template>
  <div>
    <v-dialog v-model="dialog" max-width="500px">
      <v-card>
        <v-card-title>
          <span class="headline">修改构件描述</span>
        </v-card-title>
        <v-card-text>
          <v-container grid-list-md>
            <v-form v-model="validate">
              <v-layout wrap>
                <v-flex xs12>
                  <h3>构建名：{{editedItem.name}}</h3>
                </v-flex>
                <v-flex xs12>
                  <v-text-field v-model="editedItem.describe" multi-line :rules="updateRules"
                                label="构件描述"></v-text-field>
                </v-flex>
              </v-layout>
            </v-form>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue darken-1" flat @click.native="close">Cancel</v-btn>
          <v-btn color="blue darken-1" :disabled="!validate" flat @click.native="updateComponent(editedItem)">Save
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-card>
      <v-card-title>
        <h3>检索构件</h3>
        <v-spacer></v-spacer>
        <v-text-field
          append-icon="search"
          label="构件名或构件功能描述"
          single-line
          hide-details
          v-model="search"
        ></v-text-field>
      </v-card-title>
      <v-data-table
        :headers="headers"
        :items="items"
        :loading="isLoading"
        item-key="name"
      >
        <v-progress-linear slot="progress" color="blue" indeterminate/>
        <template slot="items" slot-scope="props">
          <td class="text-xs-left">{{ props.item.name }}</td>
          <td class="text-xs-left">{{ props.item.describe }}</td>
          <td class="justify-center layout px-0">
            <v-btn icon class="mx-0" @click="editItem(props.item)">
              <v-icon color="teal">edit</v-icon>
            </v-btn>
            <v-btn icon class="mx-0" @click="deleteItem(props.item)">
              <v-icon color="pink">delete</v-icon>
            </v-btn>
          </td>
        </template>
        <template slot="pageText" slot-scope="{ pageStart, pageStop }">
          From {{ pageStart }} to {{ pageStop }}
        </template>

      </v-data-table>
    </v-card>
  </div>
</template>

<script>

  import _ from 'lodash'
  import {COMP} from '../configs/srapp.api'

  export default {
    name: "compSearch",
    data() {
      return {
        search: '',
        dialog: false,
        isLoading: false,
        updateRules: [
          (v) => v.length <= 50 || 'Input too long!',
          v => !!v || '构件描述不能为空'
        ],
        validate: false,
        defaultItem: {
          name: '',
          describe: ''
        },
        editedItem: {
          name: '',
          describe: ''
        },
        editedIndex: -1,
        pagination: {},
        headers: [
          {
            text: '构件名',
            align: 'left',
            sortable: false,
            value: 'name'
          },
          {
            text: '构件描述',
            value: 'describe',
            sortable: false,
          },
          {text: '动作', value: 'name', sortable: false}
        ],
        items: []
      }
    },
    methods: {
      editItem(component) {
        this.editedItem = Object.assign({}, component);
        this.editedIndex = this.items.indexOf(component);
        this.dialog = true;
      },
      close() {
        this.dialog = false;
        setTimeout(() => {
          this.editedItem = Object.assign({}, this.defaultItem);
          this.editedIndex = -1
        }, 300)
      },
      updateComponent(component) {
        this.$http.patch(COMP, component)
          .then(response => {
            response.json().then(result => {
              if (result['numFound'] === 1) {
                this.$message.success("构件：" + component.name + "更新成功");
                Object.assign(this.items[this.editedIndex], this.editedItem);
                this.close()
              }
            })
          }, error => {
            if (error.status === 404) {
              this.$message.error("构件：" + component.name + "不存在！")
            }
          })
      },

      isCompName(info) {
        return /^[A-Z_]+(\.[A-Za-z_]+)?$/.test(info)
      },
      forceSearchComp: function () {
        this.isLoading = true;
        let query = this.search.trim();
        if (query.length > 0) {
          if (this.isCompName(query)) {
            this.$http.get(COMP + '/' + query)
              .then(response => {
                response.json().then(result => {
                  this.items = result['comps'];
                  this.isLoading = false
                }, error => {
                  this.isLoading = false;
                  this.$message.error("发生不可知的错误，请联系管理员!")
                })
              })
          } else {
            this.$http.get(COMP, {
              params: {
                desc: this.search.trim(),
                rows: 10
              }
            }).then((response) => {
              response.json().then((result) => {
                this.items = result['comps'];
                this.isLoading = false
              })
            }, (error) => {
              this.isLoading = false;
              this.$message.error("发生不可知的错误，请联系管理员!")
            })
          }
        } else {
          this.isLoading = false;
          this.items = []
        }
      },
      searchComp: _.debounce(function () {
        this.forceSearchComp()
      }, 600),
      deleteItem(component) {
        this.$http.delete(COMP + '/' + component.name)
          .then(response => {
            if (response.status === 204) {
              this.$message.success("构件：" + component.name + "已删除。");
              // this.forceSearchComp()
              this.items.splice(this.items.indexOf(component), 1);
              this.items = Object.assign([], this.items)
            }
          })
      }
    },
    watch: {
      search(newValue, oldValue) {
        this.searchComp()
      }
    }
  }
</script>

<style scoped>

</style>
