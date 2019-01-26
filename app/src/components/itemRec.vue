<template>
  <v-card>
    <v-card-title primary-title>
      <h3>猜你喜欢</h3>
    </v-card-title>
    <v-data-table
      :headers="headers"
      :items="items"
      :loading="isLoading"
      item-key="name"
    >
      <!--<v-progress-linear slot="progress" color="blue" indeterminate/>-->
      <template slot="items" slot-scope="props">
        <td class="text-xs-left">{{ props.item.item.title }}</td>
        <td class="text-xs-left">{{ props.item.item.describe }}</td>
        <td class="text-xs-left">{{ props.item.score }}</td>
        <td class="justify-center layout px-0">
          <v-btn icon class="mx-0" @click="move(props.item.item.url)">
            <v-icon color="blue-grey darken-2">call_split</v-icon>
          </v-btn>
        </td>
      </template>
      <!--<template slot="pageText" slot-scope="{ pageStart, pageStop }">
        From {{ pageStart }} to {{ pageStop }}
      </template>-->

    </v-data-table>
  </v-card>
</template>

<script>
  import _ from 'lodash'
  import { REC, REC_LOG } from '../configs/srapp.api'

  export default {
    name: "itemRec",
    data() {
      return {
        isLoading: false,
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
            text: 'Score',
            value: 'score',
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
    mounted: function () {
      this.personalizationRec()
    },
    methods: {
      personalizationRec() {
        this.isLoading = true;
        this.$http.get(REC + "/items", {
          params: {
            userId: this.getCookie('username'),
            num: 5,
            type: 'regCorr'
          }
        }).then(response => {
          response.json().then(result => {
            this.items = result['recommendation'];
            this.isLoading = false
          })
        }, error => {
          this.isLoading = false;
          this.$message.error("Query personalization recommendation items failed.")
        })
      },
      recItems: _.debounce(function () {
        this.personalizationRec()
      }, 1000),
      getCookie(name) {
        var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
        if (arr = document.cookie.match(reg))
          return (arr[2]);
        else
          return null;
      },
      move(item) {
        this.addEvent(item);
        this.addQueryLog(item);
        this.addRecLog(item);
        window.open(item.link, '_bank')
      },
      addRecLog(item) {
        var ids = this.items.map(x => x.id);
        ids.splice(ids.indexOf(item.id), 1);
        this.$http.post(REC_LOG, {
          userId: this.getCookie('username'),
          clickItemId: item.id,
          otherItems: ids.join(',')
        }).then(v => v.json().then(result => {
          if (result['numFound'] === 1) {
            this.$message.success("Recommendation event: " + this.getCurrentUser() + " click recommendation " + item.id + " add succeed!")
          }
        }), error => {
          if (error.status === 409) {
            this.$message.error("Recommendation Event is already exist!")
          }
        });
      },
      addEvent(item) {
        var ids = this.items.map(x => x.id);
        ids.splice(ids.indexOf(item.id), 1);
        this.$http.post(EVENT, {
          userId: this.getCookie('username'),
          action: 'VIEW',
          itemId: item.id,
          otherItems: ids.join(',')
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
          resultCnt: this.items.length,
          clickItemId: item.id
        })
      }
    }
  }
</script>

<style scoped>

</style>
