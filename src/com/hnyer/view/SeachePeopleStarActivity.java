 
package com.hnyer.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hnyer.adapter.PeopleStarSortAdapter;
import com.hnyer.adapter.PeopleStarSortAdapter.ViewHolder;
import com.hnyer.bean.PinyinComparator;
import com.hnyer.bean.SortModel;
import com.hnyer.bean.SortToken;
import com.hnyer.utils.CharacterParser;
import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.opendanmaku.IDanmakuItem;
import com.xbc.contacts.R;

public class SeachePeopleStarActivity extends Activity  implements OnClickListener , PopupWindow.OnDismissListener  {

	ListView mListView;
	private List<SortModel> mAllContactsList;
	private PeopleStarSortAdapter adapter;
	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;
	
	//---------搜索框---------------------------
	private TextView tv_search;
	// show and hide
	private LinearLayout mainLayout;
	private RelativeLayout titleBarLayout;
	private int moveHeight;
	//popuwindow的y起始位置
	private int statusBarHeight;
	// search popupwindow
	private PopupWindow popupWindow;
	private View searchView;
	private EditText searchEditText;
	private TextView cancelTextView;
	//透明层
	private View alphaView;
	
	//----------弹幕-----------------
	 private DanmakuView mDanmakuView;
	 
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.seachepeoplestar_lay);
		init();
		
	}

	private void init() {
		initCtrl();
		initListener();
		loadContacts();
	}
	
	 
	private void initCtrl() {
		//弹幕
        mDanmakuView = (DanmakuView) findViewById(R.id.danmakuView);
        
		tv_search = (TextView) findViewById(R.id.tv_search);
		tv_search.setOnClickListener(this); 

		mainLayout = (LinearLayout) findViewById(R.id.serchemain_lay);   
		titleBarLayout = (RelativeLayout) findViewById(R.id.title_bar_layout);

		LayoutInflater mInflater = LayoutInflater.from(this);
		searchView = mInflater.inflate(R.layout.popup_window_search, null); 
		searchEditText = (EditText) searchView.findViewById(R.id.popup_window_et_search); 
		searchEditText.setFocusable(true);
		searchEditText.addTextChangedListener(new TextWatcher() {
 
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				String content = searchEditText.getText().toString();
				if (content.length() > 0) {
					ArrayList<SortModel> fileterList = (ArrayList<SortModel>) search(content);
					adapter.updateListView(fileterList);  
					mListView.setVisibility(View.VISIBLE);
				} else {
					
					//没有搜索时 ，隐藏列表。
                    // adapter.updateListView(mAllContactsList);
					mListView.setVisibility(View.GONE);
				}
				mListView.setSelection(0);

			
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		cancelTextView = (TextView) searchView.findViewById(R.id.popup_window_tv_cancel);
		cancelTextView.setOnClickListener(this);
		

		/** 给ListView设置adapter **/
		mListView = (ListView) searchView.findViewById(R.id.lv_contacts);
		characterParser = CharacterParser.getInstance();
		mAllContactsList = new ArrayList<SortModel>();
		pinyinComparator = new PinyinComparator(); 
		Collections.sort(mAllContactsList, pinyinComparator);// 根据a-z进行排序源数据
		adapter = new PeopleStarSortAdapter(this, mAllContactsList);
		mListView.setAdapter(adapter); 
		
		alphaView = searchView.findViewById(R.id.popup_window_v_alpha);
		alphaView.setOnClickListener(this);

		popupWindow = new PopupWindow(searchView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setTouchable(true);
		
		ColorDrawable dw = new ColorDrawable(); //实例化一个ColorDrawable颜色为半透明
		popupWindow.setBackgroundDrawable(dw);
		popupWindow.setOnDismissListener(this);
		
		
	}
	private void initListener() {
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
//				ViewHolder viewHolder = (ViewHolder) view.getTag();
//				viewHolder.cbChecked.performClick();
				adapter.toggleChecked(position);//-1是因为添加了headview
				Toast.makeText(SeachePeopleStarActivity.this, ""+adapter.getmList().get(position).sortToken.getWholeSpell(), 1).show() ; 
			}
		});

	}

 

	private void loadContacts() {
		new Thread(new Runnable() {
			@Override
			public void run() {
//				try {
				   String[] items =  SeachePeopleStarActivity.this.getResources().getStringArray(R.array.peopleStarName);
				   Log.i("hnyer", "items.size="+items.length) ;
					if (items.length > 0) {
						mAllContactsList = new ArrayList<SortModel>(); 
						for (int i = 0; i < items.length; i++) {
							String phoneNumber = "";
							String contactName = items[i];
							String sortKey = ""; 
							//中文首字母  ： A
							String sortLetters   = getSortLetter(contactName); 
							SortModel sortModel = new SortModel(contactName, phoneNumber, sortKey); 
							sortModel.sortLetters = sortLetters; 
							SortToken token = new SortToken();
							//中文名
							token.setChName(contactName) ;
							//简拼 
							token.setSimpleSpell( characterParser.getSimpPY(contactName))    ; 
							//全拼
							token.setWholeSpell( characterParser.getSelling(contactName))  ;
							sortModel.sortToken = token;
							
							
							mAllContactsList.add(sortModel);
						}
					}
					 
					runOnUiThread(new Runnable() {
						public void run() {
							Collections.sort(mAllContactsList, pinyinComparator);
							adapter.updateListView(mAllContactsList);
							setDanmuData(mAllContactsList);
						}
					});
			}
		}).start();
	}

	
	private void setDanmuData(List<SortModel> mAllContactsList){
        List<IDanmakuItem> list  = new ArrayList<IDanmakuItem>();;
        int color = Color.BLUE ;
        int  txtSize= 10 ;
        float speed = 1 ;
        
		for (SortModel mode : mAllContactsList) {

			int random = getRandomValue();
			switch (random) {
			case 1:
				color = R.color.my_item_color ;
				txtSize =20 ;
				speed = 1.5f ;
				break;
			case 2:
				color = R.color.my_item_color ;
				txtSize =25 ;
				 speed = 0.5f ;
				break;
			case 3:
				color = R.color.bg_color_deep ;
				txtSize =30 ;
				 speed = 1f ;
				break;
			case 4:
				color = R.color.blue ;
				txtSize =35 ; 
				speed = 1.2f ;
				break;

			default:
				break;
			}

			// 纯文字
			IDanmakuItem item = new DanmakuItem(this, new SpannableString(mode.sortToken.getChName()), mDanmakuView.getWidth(),0,color,0,1);
			item.setTextSize(30);
			item.setSpeedFactor(speed);
			list.add(item);

		}
        
        Collections.shuffle(list);
        mDanmakuView.addItem(list, true); 
	}
 
	
	private    int   getRandomValue() {
		// [1,5)之间的随机数
		double  result =   Math.random() * (5 - 1) + 1;
		return (int) result;
	}
	
	/**
	 * 名字转拼音,取首字母
	 * @param name
	 * @return
	 */
	private String getSortLetter(String name) {
		String letter = "#";
		if (name == null) {
			return letter;
		}
		//汉字转换成拼音
		String pinyin = characterParser.getSelling(name);
		String sortString = pinyin.substring(0, 1).toUpperCase(Locale.CHINESE);
		// 正则表达式，判断首字母是否是英文字母
		if (sortString.matches("[A-Z]")) {
			letter = sortString.toUpperCase(Locale.CHINESE);
		}
		return letter;
	}

 
	/**
	 * 模糊查询
	 * @param str
	 * @return
	 */
	private List<SortModel> search(String str) {
		List<SortModel> filterList = new ArrayList<SortModel>();//过滤后的list
		if (str.matches("^([0-9]|[/+])*$")) {//正则表达式 匹配号码
			for (SortModel contact : mAllContactsList) {
				if (contact.number != null && contact.name != null) {
					if (contact.number.contains(str) || contact.name.contains(str)) {
						if (!filterList.contains(contact)) {
							filterList.add(contact);
						}
					}
				}
			}
		} else {
			for (SortModel contact : mAllContactsList) {
				if (contact.number != null && contact.name != null) {
					//姓名全匹配,姓名首字母简拼匹配,姓名全字母匹配
					if (contact.name.toLowerCase(Locale.CHINESE).contains(str.toLowerCase(Locale.CHINESE))
							|| contact.sortKey.toLowerCase(Locale.CHINESE).replace(" ", "").contains(str.toLowerCase(Locale.CHINESE))
							|| contact.sortToken.getSimpleSpell().toLowerCase(Locale.CHINESE).contains(str.toLowerCase(Locale.CHINESE))
							|| contact.sortToken.getWholeSpell().toLowerCase(Locale.CHINESE).contains(str.toLowerCase(Locale.CHINESE))) {
						if (!filterList.contains(contact)) {
							filterList.add(contact);
						}
					}
				}
			}
		}
		return filterList;
	}

 

	private void getStatusBarHeight() {
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;
	}

	private void showSearchBar() {
		getStatusBarHeight(); 
		moveHeight = titleBarLayout.getHeight(); 
		Animation translateAnimation = new TranslateAnimation(0, 0, 0, -moveHeight);
		translateAnimation.setDuration(300);
		//整个界面网上移动 指定距离
		if(mainLayout !=null){
			Log.i("hnyer", ""+mainLayout);
			
		}else{
			Log.i("hnyer", "mainLayout= null " );
			
		}
		mainLayout.startAnimation(translateAnimation);
		
		translateAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				TranslateAnimation anim = new TranslateAnimation(0, 0, mainLayout.getY(), 0);
				mainLayout.setAnimation(anim);
				//上移动画结束后
				titleBarLayout.setVisibility(View.GONE);

				popupWindow.showAtLocation(mainLayout, Gravity.CLIP_VERTICAL, 0, statusBarHeight);
				openKeyboard();
			}
		});

	}

	private void openKeyboard() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 0);
	}

	private void dismissPopupWindow() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
		}
	}

	private void resetUI() {
		titleBarLayout.setVisibility(View.VISIBLE);
		Animation translateAnimation = new TranslateAnimation(0, 0, -moveHeight, 0);
		translateAnimation.setDuration(300);
		mainLayout.startAnimation(translateAnimation);
 
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.tv_search:
			showSearchBar();
			break;
		case R.id.popup_window_tv_cancel:
			dismissPopupWindow();
			break;
		case R.id.popup_window_v_alpha:
			dismissPopupWindow();
			break;
		}
	}

	@Override
	public void onDismiss() {
		resetUI();
	}



    @Override
    protected void onResume() {
        super.onResume();
        mDanmakuView.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDanmakuView.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDanmakuView.clear();
    }

}