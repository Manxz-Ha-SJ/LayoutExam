package com.manxz_ha_sj.layoutexam

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_layout)
        //이 메소드는 타이머가 끝나면 실행됩니다.
        Handler().postDelayed(Runnable {
            // 앱의 main activity로 넘어가기
            val i = Intent(this@IntroActivity, MainActivity::class.java)
            startActivity(i)
            // 현재 액티비티 닫기
            finish()
        }, 3000)
    }
}