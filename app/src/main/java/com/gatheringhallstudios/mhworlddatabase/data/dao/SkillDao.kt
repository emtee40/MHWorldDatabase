package com.gatheringhallstudios.mhworlddatabase.data.dao

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.gatheringhallstudios.mhworlddatabase.data.views.Skill

import com.gatheringhallstudios.mhworlddatabase.data.views.SkillTreeFull
import com.gatheringhallstudios.mhworlddatabase.data.views.SkillTreeView

/**
 * Created by Carlos on 3/21/2018.
 */
@Dao
abstract class SkillDao {
    @Query("""
        SELECT id, name, description
        FROM skilltree s join skilltree_text st USING (id)
        WHERE lang_id = :langId
        ORDER BY id ASC""")
    abstract fun loadSkillTrees(langId: String): LiveData<List<SkillTreeView>>

    /**
     * Performs a full load on a skill tree.
     */
    fun loadSkillTree(langId: String, skillTreeId: Int): LiveData<SkillTreeFull> {
        val skillFull = loadSkills(langId, skillTreeId)

        return Transformations.map(skillFull) { data ->
            val firstItem = data.first()
            val skills = data.map {
                Skill(skilltree_id = it.id, level = it.level, description = it.description)
            }

            SkillTreeFull(
                    id = firstItem.id,
                    name = firstItem.skilltree_name,
                    description = firstItem.skilltree_description,
                    skills = skills
            )
        }
    }


    // internal class used by the loadSkillTree function
    protected data class SkillWithSkillTree(
            val id: Int,
            val skilltree_name: String?,
            val skilltree_description: String?,
            val level: Int,
            val description: String?
    )

    @Query("""
        SELECT st.id, stt.name skilltree_name, stt.description skilltree_description,
            s.level, s.description
        FROM skilltree st
            JOIN skilltree_text stt
                ON stt.id = st.id
                AND stt.lang_id = :langId
            JOIN skill s
                ON s.skilltree_id = st.id
                AND s.lang_id = :langId
        WHERE st.id = :skillTreeId """)
    protected abstract fun loadSkills(langId: String, skillTreeId: Int): LiveData<List<SkillWithSkillTree>>
}