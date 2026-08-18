<script setup lang="ts">
interface RunEvent { id?: string; type?: string; status?: string; message?: string; timestamp?: string; [key:string]: unknown }
defineProps<{ events?: RunEvent[]; running?: boolean }>()
</script>
<template>
  <aside class="inspector"><header>Run inspector <span v-if="running">● running</span></header><div v-if="!events?.length" class="empty">No run events</div><ol v-else><li v-for="(event,index) in events" :key="event.id || index"><b>{{ event.type || 'event' }}</b><span>{{ event.message || event.status || '' }}</span><time v-if="event.timestamp">{{ event.timestamp }}</time></li></ol></aside>
</template>
<style scoped>.inspector{border:1px solid #e5e7eb;border-radius:10px;padding:12px;background:#fff}.inspector header{font-weight:600;margin-bottom:8px}.inspector header span{float:right;color:#2f6fed;font-size:12px}.inspector ol{margin:0;padding-left:20px}.inspector li{padding:5px 0}.inspector li span{margin-left:8px;color:#555}.inspector time{display:block;color:#999;font-size:11px}.empty{color:#999;font-size:13px}</style>
