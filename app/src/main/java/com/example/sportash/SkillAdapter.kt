package com.example.sportash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Skill(var ID: Int, var UserID: Int, var Skill: String, var EndorsmentCount: Int, var Endorsed: Boolean){}

class SkillItem(itemView: View, var isOwner: Boolean): RecyclerView.ViewHolder(itemView){
    private var sSkill: TextView? = null
    private var sSkillCount: TextView? = null
    private var sEndorsment: ImageView? = null
    private var sRemove: Button? = null

    init{
        sSkill = itemView.findViewById(R.id.skill_text)
        sSkillCount = itemView.findViewById(R.id.skill_endorsment_count)
        sEndorsment = itemView.findViewById(R.id.skill_endorsment_icon)
        sRemove = itemView.findViewById(R.id.skill_remove)
    }

    fun bind(skill: Skill, clickListener: OnSkillClickedListener){
        sSkill?.text = skill.Skill
        sSkillCount?.text = skill.EndorsmentCount.toString()
        if(isOwner) sEndorsment?.visibility = View.GONE
        else sRemove?.visibility = View.GONE
        sRemove?.setOnClickListener{
            clickListener.onRemovedClicked(skill)
        }
        if(!skill.Endorsed){
            sEndorsment?.setImageResource(R.mipmap.like_empty_foreground)
            sEndorsment?.setOnClickListener{
                clickListener.onEndorsmentClicked(skill)
                sEndorsment?.setImageResource(R.mipmap.like_full_foreground)
            }
        }
        else {
            sEndorsment?.setImageResource(R.mipmap.like_full_foreground)
            sEndorsment?.setOnClickListener{
                clickListener.onEndorsmentClicked(skill)
                sEndorsment?.setImageResource(R.mipmap.like_empty_foreground)
            }
        }
    }
}

class SkillAdapter(val skillList: MutableList<Skill>, val itemClickListener: OnSkillClickedListener, var isOwner: Boolean) : RecyclerView.Adapter<SkillItem>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.skill_item, parent, false)
        return SkillItem(view, isOwner)
    }

    override fun onBindViewHolder(holder: SkillItem, position: Int) {
        val skill = skillList[position]
        holder.bind(skill, itemClickListener)
    }

    override fun getItemCount(): Int {
        return skillList.size
    }

}

interface OnSkillClickedListener {
    fun onEndorsmentClicked(skill: Skill)
    fun onRemovedClicked(skill: Skill)
}
