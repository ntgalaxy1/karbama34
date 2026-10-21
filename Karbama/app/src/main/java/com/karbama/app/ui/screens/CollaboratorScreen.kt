package com.karbama.app.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.karbama.app.data.model.*
import com.karbama.app.ui.components.*
import com.karbama.app.ui.i18n.Loc
@Composable fun CollaboratorContent(loc:Loc,busy:Boolean,jobs:List<Job>,hotels:List<Hotel>,applicants:List<Applicant>,themeMode:String,onThemeChange:(String)->Unit,onNewJob:()->Unit,onEditJob:(Job)->Unit,onDeleteJob:(Job)->Unit,onOpenApplicant:(Applicant)->Unit,onSaveHotel:(Hotel)->Unit,onDeleteHotel:(Hotel)->Unit,onCreateApplicant:(String,String,String,String,String,String)->Unit,onExit:()->Unit){Column(Modifier.fillMaxSize()){TopBar("پنل همکار",onBack=onExit);Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)){Text("کارجوهای من");applicants.forEach{Text(it.fullName.ifBlank{it.username})};PrimaryButton("＋ ایجاد آگهی",onNewJob);jobs.forEach{TextButton(onClick={onEditJob(it)}){Text(it.company)}};HotelManagerCard(loc,hotels,onSaveHotel,onDeleteHotel);ThemeModePicker(loc,themeMode,onThemeChange)}}}
