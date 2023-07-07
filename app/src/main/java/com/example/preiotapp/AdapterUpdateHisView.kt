package com.example.test_listview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.preiotapp.R

class AdapterUpdateHisView (var context: Context, var listUpdateHis :ArrayList<UpdateHistory>):BaseAdapter() {
    class ViewHolder(row : View){
        var textviewDate :TextView
        init {
            textviewDate = row.findViewById(R.id.textViewDateID) as TextView
        }
    }

    override fun getCount(): Int {
        return listUpdateHis.size
    }

    override fun getItem(position: Int): Any {
        return listUpdateHis.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view : View?
        var viewHolder :ViewHolder
        if(convertView==null){
            var layoutInflater:LayoutInflater = LayoutInflater.from(context)
            view = layoutInflater.inflate(R.layout.item_update_history,null)
            viewHolder= ViewHolder(view)
            view.tag = viewHolder
        }else{
            view =convertView
            viewHolder = convertView.tag as ViewHolder
        }
        var updateHistory : UpdateHistory = getItem(position) as UpdateHistory
        viewHolder.textviewDate.text = updateHistory.dateUpdate
        return view as View
    }
}