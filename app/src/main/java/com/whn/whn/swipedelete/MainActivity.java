package com.whn.whn.swipedelete;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 滑动删除
 */
public class MainActivity extends AppCompatActivity {


    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.listview);
        myAdapter mAdapter = new myAdapter();
        lv.setAdapter(mAdapter);


    }


    SwipeLayout currentLayout = null;//用来记录上次打开的条目

    /**
     * 1.设置listview的Adapter
     * 2.实现接口的方法
     * 3.效果相同,new
     */
    private class myAdapter extends BaseAdapter implements SwipeLayout.onSwipeListener{

        @Override
        public int getCount() {
            return Constant.NAMES.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView==null){
                convertView  = View.inflate(parent.getContext(),R.layout.listview_item,null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tvName.setText(Constant.NAMES[position]);
            viewHolder.swipelayout.setonSwipeListener(this);//当前条目

           viewHolder.swipelayout.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
                   Log.d("MainActivity", "onClick: click");
               }
           });
            return convertView;
        }


        @Override
        public void onOpen(SwipeLayout swipeLayout) {
            Toast.makeText(MainActivity.this, "open", Toast.LENGTH_SHORT).show();
            //删除上次打开的
            if(currentLayout!=null && currentLayout!=swipeLayout){
                currentLayout.close();
            }
            //记录打开的
            currentLayout = swipeLayout;
        }


        @Override
        public void onClose(SwipeLayout swipeLayout) {
            Toast.makeText(MainActivity.this, "close", Toast.LENGTH_SHORT).show();
            if(currentLayout==swipeLayout){
                currentLayout=null;
            }
        }

        @Override
        public void onTouchDown(SwipeLayout swipeLayout) {
            Toast.makeText(MainActivity.this, "down", Toast.LENGTH_SHORT).show();
            if(currentLayout!=null && currentLayout!=swipeLayout){
                currentLayout.close();
            }
        }


    }
    static class ViewHolder {
        @InjectView(R.id.tv_delete)
        TextView tvDelete;
        @InjectView(R.id.tv_name)
        TextView tvName;
        @InjectView(R.id.swipelayout)
        SwipeLayout swipelayout;
        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }




}
