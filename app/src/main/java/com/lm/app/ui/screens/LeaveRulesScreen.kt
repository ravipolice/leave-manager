package com.lm.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LeaveRule(
    val id: Int,
    val name: String,
    val description: String,
    val ruleDetails: String
)

val leaveRulesData = listOf(
    LeaveRule(
        1, "Earned Leave", "Can be availed on personal or Medical Grounds",
        "Subject to the provisions of rule 9, and sub-rule (2) of this rule, the maximum earned leave that can be granted to a member of the service at a time shall be 180 days: ..."
    ),
    LeaveRule(
        2, "Half Pay Leave", "Can be availed on personal or Medical Grounds",
        "12(1) The half pay leave account of every member of Service shall subject to the provisions of sub-rule (2), be credited with half pay leave in advance in two installments of ten days each on the first day of January and July of every calendar year. ..."
    ),
    LeaveRule(
        3, "Commuted Leave", "Can be availed only on Medical Grounds by producing medical certificate",
        "13(1)Commuted leave not exceeding half the amount of half pay leave due may be granted on medical certificate to a member of the Service subject to the condition that twice the amount of such leave shall be debitable to the half pay leave due. ..."
    ),
    LeaveRule(
        4, "Leave Not Due", "Not exceeding 360 days during his entire service on medical certificate.",
        "14. Leave not due—Save in the case of leave preparatory to retirement leave not due may be granted to a member of the Service for a period not exceeding 360 days during his entire service 26[ ] on medical certificate. ..."
    ),
    LeaveRule(
        5, "Extraordinary Leave", "May be granted to a member of the Service under special circumstances",
        "15 (1) Subject to the provisions of rule 7, extraordinary leave may be granted to a member of the Service in the following special circumstances, that is to say— ..."
    ),
    LeaveRule(
        6, "Special Disability leave", "May be combined with leave of any other kind, under rule 16 (1)",
        "16 (1) Special disability leave, which may be combined with leave of any other kind, may be granted to a member of the Service under such conditions as may be prescribed in the regulations made in this behalf by the Central Government in consultation with the State Governments concerned. ..."
    ),
    LeaveRule(
        7, "Maternity Leave", "Applicable for 2 Children 180 days each under AIS (Leave rules) 1955 sub rule 18(1)",
        "18(1) Maternity leave may be granted to a woman member of the Service with less than two surviving children on full pay up to a period of 180* days from the date of its commencement. During such period, she shall be paid leave salary equal to the pay drawn immediately before proceeding on leave. ..."
    ),
    LeaveRule(
        8, "Paternity Leave", "15 days within 6 months of Child Birth as per sub rule 18 (b) only for 2 children's",
        "(1) A male member of the Service (including a probationer) with less than two surviving children, may be granted paternity leave by an authority competent to grant leave for a period of 15 days, during the confinement of his wife for childbirth, i.e. up to 15 days before, or up to six months from the date of delivery of the child. ..."
    ),
    LeaveRule(
        9, "Paternity leave for Child adoption", "15 days within 6 months of Child Birth as per sub rule 18 (c)",
        "(1) A male member of the Service (including a probationer) with less than two surviving children, on valid adoption of a child below the age of one year, may be granted Paternity Leave by the competent authority for a period of 15 days, within a period of six months from the date of such adoption: ..."
    ),
    LeaveRule(
        10, "Child Care Leave", "730 days combined for 2 children up to their age of 18 (only of female officers) as per sub rule 18 (D)",
        "(1) A female member of the Service having minor children below the age of eighteen years may be granted child care leave by the competent authority for a maximum of 730 days during her entire service for taking care of up to two children. ..."
    ),
    LeaveRule(
        11, "Conversion of one kind of leave in to another", "May convert any kind of leave retrospectively into leave of a different kind, which may be admissible",
        "19 (1) At the request of a member of the Service, the Government may convert any kind of leave retrospectively into leave of a different kind, which may be admissible, but the member of the Service cannot claim such conversion as a matter of right.\n\n19 (2) If one kind of leave is converted into another the amount of leave salary admissible shall be recalculated and arrears of leave salary paid or amounts overdrawn recovered, as the case may be."
    ),
    LeaveRule(
        12, "Leave Salary", "A member of the Service on earned leave is entitled to leave salary equal to the pay drawn immediately before proceeding on earned leave",
        "20 (1) A member of the Service on earned leave is entitled to leave salary equal to the pay drawn immediately before proceeding on earned leave,\n20 (2) A member of the Service on half pay leave or leave not due is entitled to leave salary equal to half the amount specified in sub-rule (1). ..."
    ),
    LeaveRule(
        13, "Maximum Period of absence from Duty", "No member of the Service shall be granted leave of any kind for a continuous period exceeding five years.",
        "7. Maximum period of absence from duty—(1) No member of the Service shall be granted leave of any kind for a continuous period exceeding five years. ..."
    ),
    LeaveRule(
        14, "Combination of Leave", "Except as otherwise provided in these rules, any kind of leave under these rules may be granted in combination with or in continuation of any other kind of leave",
        "8. Combination of leave—Except as otherwise provided in these rules, any kind of leave under these rules may be granted in combination with or in continuation of any other kind of leave."
    ),
    LeaveRule(
        15, "Encashment of earned leave at the time of availing Leave Travel Concession", "Encashment of earned leave at the time of availing Leave Travel Concession",
        "20(C) Encashment of earned leave at the time of availing Leave Travel Concession —\n\n(1) A member of the Service may be sanctioned encashment of ten days of earned leave out of the total earned leave at his credit while availing leave travel concession if — ..."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRulesScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Rules & Provisions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(leaveRulesData) { rule ->
                LeaveRuleItem(rule)
            }
        }
    }
}

@Composable
fun LeaveRuleItem(rule: LeaveRule) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${rule.id}. ${rule.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = rule.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = rule.ruleDetails,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
