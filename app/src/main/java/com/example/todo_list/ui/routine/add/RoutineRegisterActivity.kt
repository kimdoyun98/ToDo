package com.example.todo_list.ui.routine.add

import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.TimePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.todo_list.R
import com.example.todo_list.alarm.Alarm
import com.example.todo_list.data.room.RoutineEntity
import com.example.todo_list.databinding.ActivityCycleRegisterBinding
import com.example.todo_list.ui.routine.RoutineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class RoutineRegisterActivity : AppCompatActivity(), TimePicker.OnTimeChangedListener {
    private val viewModel : RoutineViewModel by viewModels()
    private lateinit var binding : ActivityCycleRegisterBinding
    @Inject lateinit var alarm: Alarm
    private var checkedDayList = MutableList(7) {false}
    private val time = Array(2){-1}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCycleRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Toggle
         */
        val toggle = arrayOf(binding.sunday, binding.monday, binding.tuesday,
            binding.wednesday, binding.thursday, binding.friday, binding.saturday
        )

        toggle.forEach{
            it.setOnCheckedChangeListener(DayToggle())
        }

        /**
         * TimePicker
         */
        binding.timePicker.setOnTimeChangedListener(this)


        /**
         * 취소 & 등록
         */
        binding.cycleCancel.setOnClickListener{
            finish()
        }

        binding.cycleRegister.setOnClickListener{
            if(time.contains(-1)){
                val cal = Calendar.getInstance()
                time[0] = cal.get(Calendar.HOUR_OF_DAY)
                time[1] = cal.get(Calendar.MINUTE)
            }
            val time2 = time.map { "%02d".format(it) }
            viewModel.insert(
                RoutineEntity(
                    title  = binding.title.text.toString(),
                    day = checkedDayList,
                    success = false,
                    time = "${time2[0]}:${time2[1]}"
                )
            )

            /**
             * 알림 등록
             */
            //alarmCode는 기본키로 하여 중첩 방지해야 함
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                    launch {
                        try{
                            viewModel.getId(binding.title.text.toString()).collect{
                                if(it != -1) {
                                    alarm.setAlarm(
                                            time2[0].toInt(),
                                            time2[1].toInt(),
                                            it,
                                            binding.title.text.toString(),
                                            checkedDayList
                                        )
                                }
                            }
                        }catch (e: Throwable){
                            Log.e("RoutineRegisterActivity", e.message.toString())
                        }
                    }
                }
            }

            finish()
        }
    }
    inner class DayToggle: CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            when(buttonView?.id){
                binding.sunday.id -> {
                    checkedDayList[0] = isChecked
                }
                binding.monday.id -> {
                    checkedDayList[1] = isChecked
                }
                binding.tuesday.id -> {
                    checkedDayList[2] = isChecked
                }
                binding.wednesday.id -> {
                    checkedDayList[3] = isChecked
                }
                binding.thursday.id -> {
                    checkedDayList[4] = isChecked
                }
                binding.friday.id -> {
                    checkedDayList[5] = isChecked
                }
                binding.saturday.id -> {
                    checkedDayList[6] = isChecked
                }
            }
            var text = getString(R.string.empty_string)
            if (checkedDayList.count{ it } == 7) text = getString(R.string.daily)
            else {
                for (i in 0 until checkedDayList.size) {
                    if (checkedDayList[i]) {
                        text += resources.getStringArray(R.array.Days)[i]
                    }
                }
            }
            binding.choiceDay.text = text
        }

    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        time[0] = hourOfDay
        time[1] = minute
    }
}