package com.karbama.app.ui.components

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import com.karbama.app.data.model.Status
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.R
import com.karbama.app.ui.theme.*


// ==================== پس‌زمینه‌ی مِش رنگی (دیزاین v3) ====================

/**
 * مِش رنگی + هاله‌های نرم زیر همه‌ی صفحه‌ها.
 * روی ریشه‌ی برنامه یک‌بار اعمال می‌شود و صفحه‌ها روی آن شفاف‌اند.
 */
fun Modifier.brandBackdrop(): Modifier = this.drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            0f to Surface,
            0.5f to Surface,
            1f to VioletLight
        )
    )

    fun orb(cx: Float, cy: Float, r: Float, color: Color, a: Float) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = a), color.copy(alpha = 0f)),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r,
            center = Offset(cx, cy)
        )
    }

    val w = size.width
    val h = size.height
    orb(w * 1.02f, h * 0.02f, w * 0.80f, Mint, 0.40f)
    orb(w * -0.06f, h * 0.24f, w * 0.78f, Violet, 0.26f)
    orb(w * 0.99f, h * 0.72f, w * 0.76f, Coral, 0.22f)
    orb(w * 0.08f, h * 1.00f, w * 0.72f, Sun, 0.24f)
}

// ==================== لوگو ====================

@Composable
fun BrandLogo(size: Dp = 78.dp, onDark: Boolean = false, modifier: Modifier = Modifier) {
    // لوگوی رسمی برنامه (تصویر) — روی هر پس‌زمینه‌ای گرد و تمیز دیده می‌شود
    Box(
        modifier = modifier
            .size(size)
            .shadow(if (onDark) 10.dp else 6.dp, CircleShape)
            .clip(CircleShape)
            .border(2.dp, Color.White.copy(alpha = if (onDark) 0.9f else 0.7f), CircleShape)
    ) {
        Image(
            painter = painterResource(R.drawable.karbama_logo),
            contentDescription = "Karbama",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

// ==================== نوار بالا ====================

@Composable
fun TopBar(
    title: String,
    subtitle: String? = null,
    subtitleColor: Color = Ink3,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = GlassBar) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .defaultMinSize(minHeight = 56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Ink)
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        fontWeight = if (subtitleColor == Graphite) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            trailing()
        }
        HorizontalDivider(thickness = 1.dp, color = Line)
    }
}

/** نوار بالای صفحه‌های دارای هدر رنگی */
@Composable
fun TealTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.linearGradient(listOf(BrandTeal, BrandGreen, BrandDeep)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .defaultMinSize(minHeight = 58.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            trailing()
        }
    }
}

/** تنظیم رنگ آیکن‌های نوار وضعیت بر اساس پس‌زمینه */
@Composable
fun SystemBars(darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = darkIcons
                isAppearanceLightNavigationBars = darkIcons
            }
        }
    }
}

// ==================== دکمه‌ها ====================

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    val shape = MaterialTheme.shapes.medium
    val active = enabled && !loading
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .shadow(if (active) 12.dp else 0.dp, shape)
            .clip(shape)
            .background(
                if (active) Brush.verticalGradient(listOf(BrandTeal, BrandDeep))
                else Brush.verticalGradient(listOf(Brand.copy(alpha = 0.45f), Brand.copy(alpha = 0.45f)))
            )
            .clickable(enabled = active) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
        border = androidx.compose.foundation.BorderStroke(1.4.dp, Line)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = DangerLight,
            contentColor = Danger
        ),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Danger.copy(alpha = 0.35f))
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

// ==================== کارت و عنوان بخش ====================

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = GlassFill,
        border = BorderStroke(1.dp, GlassStroke),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Brand)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Ink2,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink3)
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
fun Pill(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = fg)
    }
}

@Composable
fun StatusBadge(status: String, loc: Loc) {
    val (bg, fg) = when (status) {
        Status.REVIEWED -> AccentLight to WarnText
        Status.REFERRED -> OkLight to OkDeep
        else -> BlueLight to Blue
    }
    Pill(text = loc.status(status), bg = bg, fg = fg)
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 54.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 38.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onAction,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandDark)
            ) { Text(actionLabel, style = MaterialTheme.typography.labelMedium) }
        }
    }
}

@Composable
fun LoadingBox(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Brand, strokeWidth = 3.dp)
        Spacer(Modifier.height(12.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

// ==================== فیلدهای فرم ====================

@Composable
fun KbField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    minLines: Int = 1,
    required: Boolean = false,
    enabled: Boolean = true,
    password: Boolean = false,
    prefix: String? = null,
    revealable: Boolean = true
) {
    var revealed by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (required) "$label *" else label,
            style = MaterialTheme.typography.labelMedium,
            color = Ink2,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            enabled = enabled,
            isError = error != null,
            placeholder = placeholder?.let {
                { Text(it, color = Ink3, style = MaterialTheme.typography.bodyMedium) }
            },
            prefix = prefix?.let {
                {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandDark
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.medium,
            visualTransformation = if (password && !revealed) PasswordVisualTransformation()
            else VisualTransformation.None,
            trailingIcon = if (password && revealable) {
                {
                    // #24: دکمه‌ی چشم جمع‌وجمع (۳۸dp) تا در فیلدهای باریک، متن کم‌جا نشود
                    IconButton(
                        onClick = { revealed = !revealed },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (revealed) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Ink3,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = FieldBg,
                disabledContainerColor = FieldBg,
                focusedBorderColor = Brand,
                unfocusedBorderColor = Line,
                cursorColor = Brand
            )
        )
        if (error != null) {
            Text(
                text = error,
                color = Danger,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

/** فیلد انتخابی با پنجره‌ی فهرست (برای شهر، منطقه، عنوان شغلی و…) */
@Composable
fun PickerField(
    label: String,
    value: String,
    placeholder: String,
    options: List<Pair<String, String>>,   // value to label
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    required: Boolean = false,
    error: String? = null,
    searchable: Boolean = false,
    searchPlaceholder: String = "",
    emptyOptionLabel: String? = null
) {
    var open by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (required) "$label *" else label,
            style = MaterialTheme.typography.labelMedium,
            color = Ink2,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(if (enabled) FieldBg else FieldBg.copy(alpha = 0.5f))
                .clickable(enabled = enabled) {
                    query = ""
                    open = true
                }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifEmpty { placeholder },
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isEmpty()) Ink3 else Ink,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = Ink3)
        }
        if (error != null) {
            Text(
                text = error,
                color = Danger,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }

    if (open) {
        val filtered = if (query.isBlank()) options
        else options.filter { it.second.contains(query, ignoreCase = true) }
        Dialog(onDismissRequest = { open = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = GlassBar,
                border = BorderStroke(1.dp, GlassStroke),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (searchable) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text(searchPlaceholder, color = Ink3, fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = FieldBg,
                                focusedContainerColor = CardBg,
                                focusedBorderColor = Brand,
                                unfocusedBorderColor = Line
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        if (emptyOptionLabel != null) {
                            item {
                                PickerRow(emptyOptionLabel, value.isEmpty()) {
                                    onSelect("")
                                    open = false
                                }
                            }
                        }
                        items(filtered) { (v, l) ->
                            PickerRow(l, v == value) {
                                onSelect(v)
                                open = false
                            }
                        }
                        if (filtered.isEmpty()) {
                            item {
                                Text(
                                    "—",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(if (selected) BrandLight else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) BrandDark else Ink,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = BrandDark)
    }
}

@Composable
fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) BrandDark else Ink2,
                maxLines = 1
            )
        },
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Line,
            selectedBorderColor = Brand,
            borderWidth = 1.4.dp,
            selectedBorderWidth = 1.4.dp
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = FieldBg,
            labelColor = Ink2,
            selectedContainerColor = BrandLight,
            selectedLabelColor = BrandDark
        )
    )
}

@Composable
fun SelectionSummary(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = BrandDark,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun HorizontalChipRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

// برای دسترسی راحت در بعضی صفحات
@Composable
fun VerticalScrollColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        content = content
    )
}

@Composable
fun SecondaryArrowForward(modifier: Modifier = Modifier) {
    Icon(
        Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        tint = BrandDark,
        modifier = modifier
    )
}

@Composable
fun CloseIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Close, contentDescription = null, tint = Ink2)
    }
}

/** مسیر عکس را به مدل قابل‌نمایش برای Coil تبدیل می‌کند (فایل محلی یا آدرس اینترنتی) */
fun photoModel(path: String): Any? = when {
    path.isBlank() -> null
    path.startsWith("/") -> java.io.File(path)
    else -> path
}
