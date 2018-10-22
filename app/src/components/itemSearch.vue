<template>
  <div>
    <v-dialog v-model="dialog" max-width="500px">
      <v-card>
        <v-card-title>
          <span class="headline">Edit Item Description</span>
        </v-card-title>
        <v-card-text>
          <v-container grid-list-md>
            <v-form v-model="validate">
              <v-layout wrap>
                <v-flex xs12>
                  <h3>Item Title：{{editedItem.title}}</h3>
                </v-flex>
                <v-flex xs12>
                  <v-text-field v-model="editedItem.describe" multi-line :rules="updateRules"
                                label="Item Description"></v-text-field>
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
        <h3>检索Item</h3>
        <v-spacer></v-spacer>
        <v-text-field
          append-icon="search"
          label="Item title or description"
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
          <td class="text-xs-left">{{ props.item.title }}</td>
          <td class="text-xs-left">{{ props.item.describe }}</td>
          <td class="justify-center layout px-0">
            <v-btn icon class="mx-0" @click="editItem(props.item)">
              <v-icon color="teal">edit</v-icon>
            </v-btn>
            <v-btn icon class="mx-0" @click="deleteItem(props.item)">
              <v-icon color="pink">delete</v-icon>
            </v-btn>
            <v-btn icon class="mx-0" @click="move(props.item)">
              <v-icon color="blue-grey darken-2">call_split</v-icon>
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
  import { ITEM, EVENT, QUERY_LOG } from '../configs/srapp.api'

  export default {
    name: "itemSearch",
    data() {
      return {
        search: '',
        dialog: false,
        isLoading: false,
        updateRules: [
          (v) => v.length <= 50 || 'Input too long!',
          v => !!v || 'Item title cannot be empty!'
        ],
        validate: false,
        defaultItem: {
          title: '',
          describe: '',
          link: ''
        },
        editedItem: {
          title: '',
          describe: '',
          link: ''
        },
        editedIndex: -1,
        pagination: {},
        headers: [
          {
            text: 'Item Title',
            align: 'left',
            sortable: false,
            value: 'title'
          },
          {
            text: 'Item Description',
            value: 'describe',
            sortable: false
          },
          {
            text: 'Item Link',
            value: 'link',
            sortable: false
          }
        ],
        items: []
      }
    },
    methods: {
      editItem(item) {
        this.editedItem = Object.assign({}, item);
        this.editedIndex = this.items.indexOf(item);
        this.dialog = true;
      },
      close() {
        this.dialog = false;
        setTimeout(() => {
          this.editedItem = Object.assign({}, this.defaultItem);
          this.editedIndex = -1
        }, 300)
      },
      updateItem(item) {
        this.$http.patch(ITEM, item)
          .then(response => {
            response.json().then(result => {
              if (result['numFound'] === 1) {
                this.$message.success("Item: " + item.title + " update succeed!")
                Object.assign(this.items[this.editedIndex], this.editedItem)
              }
            })
          }, error => {
            if (error.status === 404) {
              this.$message.error("Item: " + item.title + " not exist! ")
            }
          })
      },
      isNumber(info) {
        return /^[0-9]+$/.test(info)
      },
      forceSearchItem: function () {
        this.isLoading = true;
        let query = this.search.trim();
        if (query.length > 0) {
          if (this.isNumber(query)) {
            console.log(ITEM + '/' + query)
            this.$http.get(ITEM + '/' + query)
              .then(response => {
                response.json().then(result => {
                  this.items = result['items'];
                  this.isLoading = false
                })
              }, error => {
                this.isLoading = false;
                this.$message.error("Query Item information failed.")
              })
          } else {
            this.$http.get(ITEM, {
              params: {
                query: this.search.trim(),
                rows: 10
              }
            }).then(response => {
              response.json().then(result => {
                this.items = result['items'];
                this.isLoading = false
              })
            }, error => {
              this.isLoading = false;
              this.$message.error("Query Item information failed.")
            })
          }
        } else {
          this.isLoading = false;
          this.items = []
        }
      },
      searchItem: _.debounce(function () {
        this.forceSearchItem()
      }, 1000),
      deleteItem(item) {
        this.$http.delete(ITEM + '/' + item.id)
          .then(response => {
            if (response.status === 204) {
              this.$message.success("Item: " + item.id + " has been deleted.");
              this.items.splice(this.items.indexOf(item), 1);
              this.items = Object.assign([], this.items)
            }
          })
      },
      move(item) {
        this.addEvent(item);
        this.addQueryLog(item);
        window.open(item.link, '_bank')
      },
      getCookie(name) {
        var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
        if (arr = document.cookie.match(reg))
          return (arr[2]);
        else
          return null;
      },
      addEvent(item) {
        this.$http.post(EVENT, {
          userId: this.getCookie('username'),
          action: 'VIEW',
          itemId: item.id
        }).then(v => v.json().then(result => {
          if (result['numFound'] === 1) {
            this.$message.success("Event: " + this.getCurrentUser() + " view " + item.id + " add succeed!")
          }
        }), error => {
          if (error.status === 409) {
            this.$message.error("Event is already exist!")
          }
        });
      },
      addQueryLog(item) {
        this.$http.post(QUERY_LOG, {
          userId: this.getCookie('username'),
          query: this.search.trim(),
          resultCnt: thi.items.size(),
          clickItemId: item.id
        })
      }
    },
    watch: {
      search(newValue, oldValue) {
        this.searchItem()
      }
    }
  }
</script>

<style scoped>

</style>
