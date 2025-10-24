package com.tiarkaerell.ibstracker.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom Material Design Icons (MDI) for food categories
 * Source: https://pictogrammers.com/library/mdi/
 */
object CustomIcons {

    /**
     * MDI Barley icon for grains/wheat
     * https://pictogrammers.com/library/mdi/icon/barley/
     */
    val Barley: ImageVector
        get() {
            if (_barley != null) {
                return _barley!!
            }
            _barley = ImageVector.Builder(
                name = "Barley",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(7.33f, 18.33f)
                    curveTo(6.5f, 17.17f, 6.5f, 15.83f, 6.5f, 14.5f)
                    curveTo(8.17f, 15.5f, 9.83f, 16.5f, 10.67f, 17.67f)
                    lineTo(11f, 18.23f)
                    verticalLineTo(15.95f)
                    curveTo(9.5f, 15.05f, 8.08f, 14.13f, 7.33f, 13.08f)
                    curveTo(6.5f, 11.92f, 6.5f, 10.58f, 6.5f, 9.25f)
                    curveTo(8.17f, 10.25f, 9.83f, 11.25f, 10.67f, 12.42f)
                    lineTo(11f, 13f)
                    verticalLineTo(10.7f)
                    curveTo(9.5f, 9.8f, 8.08f, 8.88f, 7.33f, 7.83f)
                    curveTo(6.5f, 6.67f, 6.5f, 5.33f, 6.5f, 4f)
                    curveTo(8.17f, 5f, 9.83f, 6f, 10.67f, 7.17f)
                    curveTo(10.77f, 7.31f, 10.86f, 7.46f, 10.94f, 7.62f)
                    curveTo(10.77f, 7f, 10.66f, 6.42f, 10.65f, 5.82f)
                    curveTo(10.64f, 4.31f, 11.3f, 2.76f, 11.96f, 1.21f)
                    curveTo(12.65f, 2.69f, 13.34f, 4.18f, 13.35f, 5.69f)
                    curveTo(13.36f, 6.32f, 13.25f, 6.96f, 13.07f, 7.59f)
                    curveTo(13.15f, 7.45f, 13.23f, 7.31f, 13.33f, 7.17f)
                    curveTo(14.17f, 6f, 15.83f, 5f, 17.5f, 4f)
                    curveTo(17.5f, 5.33f, 17.5f, 6.67f, 16.67f, 7.83f)
                    curveTo(15.92f, 8.88f, 14.5f, 9.8f, 13f, 10.7f)
                    verticalLineTo(13f)
                    lineTo(13.33f, 12.42f)
                    curveTo(14.17f, 11.25f, 15.83f, 10.25f, 17.5f, 9.25f)
                    curveTo(17.5f, 10.58f, 17.5f, 11.92f, 16.67f, 13.08f)
                    curveTo(15.92f, 14.13f, 14.5f, 15.05f, 13f, 15.95f)
                    verticalLineTo(18.23f)
                    lineTo(13.33f, 17.67f)
                    curveTo(14.17f, 16.5f, 15.83f, 15.5f, 17.5f, 14.5f)
                    curveTo(17.5f, 15.83f, 17.5f, 17.17f, 16.67f, 18.33f)
                    curveTo(15.92f, 19.38f, 14.5f, 20.3f, 13f, 21.2f)
                    verticalLineTo(23f)
                    horizontalLineTo(11f)
                    verticalLineTo(21.2f)
                    curveTo(9.5f, 20.3f, 8.08f, 19.38f, 7.33f, 18.33f)
                    close()
                }
            }.build()
            return _barley!!
        }

    private var _barley: ImageVector? = null

    /**
     * MDI Cow icon for dairy
     * https://pictogrammers.com/library/mdi/icon/cow/
     */
    val Cow: ImageVector
        get() {
            if (_cow != null) {
                return _cow!!
            }
            _cow = ImageVector.Builder(
                name = "Cow",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(10.5f, 18f)
                    arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 18.5f)
                    arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.5f, 19f)
                    arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10f, 18.5f)
                    arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.5f, 18f)
                    moveTo(13.5f, 18f)
                    arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 18.5f)
                    arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.5f, 19f)
                    arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13f, 18.5f)
                    arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.5f, 18f)
                    moveTo(10f, 11f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 12f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10f, 13f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9f, 12f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10f, 11f)
                    moveTo(14f, 11f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 12f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 13f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13f, 12f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 11f)
                    moveTo(18f, 18f)
                    curveTo(18f, 20.21f, 15.31f, 22f, 12f, 22f)
                    curveTo(8.69f, 22f, 6f, 20.21f, 6f, 18f)
                    curveTo(6f, 17.1f, 6.45f, 16.27f, 7.2f, 15.6f)
                    curveTo(6.45f, 14.6f, 6f, 13.35f, 6f, 12f)
                    lineTo(6.12f, 10.78f)
                    curveTo(5.58f, 10.93f, 4.93f, 10.93f, 4.4f, 10.78f)
                    curveTo(3.38f, 10.5f, 1.84f, 9.35f, 2.07f, 8.55f)
                    curveTo(2.3f, 7.75f, 4.21f, 7.6f, 5.23f, 7.9f)
                    curveTo(5.82f, 8.07f, 6.45f, 8.5f, 6.82f, 8.96f)
                    lineTo(7.39f, 8.15f)
                    curveTo(6.79f, 7.05f, 7f, 4f, 10f, 3f)
                    lineTo(9.91f, 3.14f)
                    verticalLineTo(3.14f)
                    curveTo(9.63f, 3.58f, 8.91f, 4.97f, 9.67f, 6.47f)
                    curveTo(10.39f, 6.17f, 11.17f, 6f, 12f, 6f)
                    curveTo(12.83f, 6f, 13.61f, 6.17f, 14.33f, 6.47f)
                    curveTo(15.09f, 4.97f, 14.37f, 3.58f, 14.09f, 3.14f)
                    lineTo(14f, 3f)
                    curveTo(17f, 4f, 17.21f, 7.05f, 16.61f, 8.15f)
                    lineTo(17.18f, 8.96f)
                    curveTo(17.55f, 8.5f, 18.18f, 8.07f, 18.77f, 7.9f)
                    curveTo(19.79f, 7.6f, 21.7f, 7.75f, 21.93f, 8.55f)
                    curveTo(22.16f, 9.35f, 20.62f, 10.5f, 19.6f, 10.78f)
                    curveTo(19.07f, 10.93f, 18.42f, 10.93f, 17.88f, 10.78f)
                    lineTo(18f, 12f)
                    curveTo(18f, 13.35f, 17.55f, 14.6f, 16.8f, 15.6f)
                    curveTo(17.55f, 16.27f, 18f, 17.1f, 18f, 18f)
                    moveTo(12f, 16f)
                    curveTo(9.79f, 16f, 8f, 16.9f, 8f, 18f)
                    curveTo(8f, 19.1f, 9.79f, 20f, 12f, 20f)
                    curveTo(14.21f, 20f, 16f, 19.1f, 16f, 18f)
                    curveTo(16f, 16.9f, 14.21f, 16f, 12f, 16f)
                    moveTo(12f, 14f)
                    curveTo(13.12f, 14f, 14.17f, 14.21f, 15.07f, 14.56f)
                    curveTo(15.65f, 13.87f, 16f, 13f, 16f, 12f)
                    arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 8f)
                    arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 12f)
                    curveTo(8f, 13f, 8.35f, 13.87f, 8.93f, 14.56f)
                    curveTo(9.83f, 14.21f, 10.88f, 14f, 12f, 14f)
                    moveTo(14.09f, 3.14f)
                    verticalLineTo(3.14f)
                    close()
                }
            }.build()
            return _cow!!
        }

    private var _cow: ImageVector? = null

    /**
     * MDI Food Apple icon for fruits
     * https://pictogrammers.com/library/mdi/icon/food-apple/
     */
    val FoodApple: ImageVector
        get() {
            if (_foodApple != null) {
                return _foodApple!!
            }
            _foodApple = ImageVector.Builder(
                name = "FoodApple",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(20f, 10f)
                    curveTo(22f, 13f, 17f, 22f, 15f, 22f)
                    curveTo(13f, 22f, 13f, 21f, 12f, 21f)
                    curveTo(11f, 21f, 11f, 22f, 9f, 22f)
                    curveTo(7f, 22f, 2f, 13f, 4f, 10f)
                    curveTo(6f, 7f, 9f, 7f, 11f, 8f)
                    verticalLineTo(5f)
                    curveTo(5.38f, 8.07f, 4.11f, 3.78f, 4.11f, 3.78f)
                    curveTo(4.11f, 3.78f, 6.77f, 0.19f, 11f, 5f)
                    verticalLineTo(3f)
                    horizontalLineTo(13f)
                    verticalLineTo(8f)
                    curveTo(15f, 7f, 18f, 7f, 20f, 10f)
                    close()
                }
            }.build()
            return _foodApple!!
        }

    private var _foodApple: ImageVector? = null

    /**
     * MDI Food Drumstick icon for proteins/meat
     * https://pictogrammers.com/library/mdi/icon/food-drumstick/
     */
    val FoodDrumstick: ImageVector
        get() {
            if (_foodDrumstick != null) {
                return _foodDrumstick!!
            }
            _foodDrumstick = ImageVector.Builder(
                name = "FoodDrumstick",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(20.16f, 12.73f)
                    curveTo(22.93f, 9.96f, 22.57f, 5.26f, 19.09f, 3f)
                    curveTo(17.08f, 1.67f, 14.39f, 1.66f, 12.36f, 2.97f)
                    curveTo(10.6f, 4.1f, 9.63f, 5.86f, 9.46f, 7.68f)
                    curveTo(9.33f, 9f, 8.83f, 10.23f, 7.91f, 11.15f)
                    lineTo(7.88f, 11.18f)
                    curveTo(6.72f, 12.34f, 6.72f, 14.11f, 7.81f, 15.19f)
                    lineTo(8.8f, 16.18f)
                    curveTo(9.89f, 17.27f, 11.66f, 17.27f, 12.75f, 16.18f)
                    curveTo(13.72f, 15.21f, 15f, 14.68f, 16.39f, 14.53f)
                    curveTo(17.76f, 14.38f, 19.1f, 13.78f, 20.16f, 12.73f)
                    moveTo(6.26f, 19.86f)
                    curveTo(6.53f, 20.42f, 6.44f, 21.1f, 5.97f, 21.56f)
                    curveTo(5.39f, 22.15f, 4.44f, 22.15f, 3.85f, 21.56f)
                    curveTo(3.58f, 21.29f, 3.44f, 20.94f, 3.42f, 20.58f)
                    curveTo(3.06f, 20.56f, 2.71f, 20.42f, 2.44f, 20.15f)
                    curveTo(1.85f, 19.56f, 1.85f, 18.61f, 2.44f, 18.03f)
                    curveTo(2.9f, 17.57f, 3.59f, 17.47f, 4.14f, 17.74f)
                    lineTo(6.62f, 15.31f)
                    curveTo(6.76f, 15.5f, 6.92f, 15.72f, 7.1f, 15.9f)
                    lineTo(8.09f, 16.89f)
                    curveTo(8.3f, 17.09f, 8.5f, 17.26f, 8.76f, 17.41f)
                    lineTo(6.26f, 19.86f)
                    close()
                }
            }.build()
            return _foodDrumstick!!
        }

    private var _foodDrumstick: ImageVector? = null

    /**
     * MDI Peanut icon for nuts & seeds
     * https://pictogrammers.com/library/mdi/icon/peanut/
     */
    val Peanut: ImageVector
        get() {
            if (_peanut != null) {
                return _peanut!!
            }
            _peanut = ImageVector.Builder(
                name = "Peanut",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 3f)
                    curveTo(8.13f, 3f, 5f, 5.69f, 5f, 9f)
                    curveTo(5f, 10.11f, 5.29f, 11.15f, 5.79f, 12.06f)
                    curveTo(5.29f, 12.97f, 5f, 14f, 5f, 15f)
                    curveTo(5f, 18.31f, 8.13f, 21f, 12f, 21f)
                    curveTo(15.87f, 21f, 19f, 18.31f, 19f, 15f)
                    curveTo(19f, 14f, 18.71f, 12.97f, 18.21f, 12.06f)
                    curveTo(18.71f, 11.15f, 19f, 10.11f, 19f, 9f)
                    curveTo(19f, 5.69f, 15.87f, 3f, 12f, 3f)
                    moveTo(10.5f, 7.5f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.5f, 8.5f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.5f, 9.5f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.5f, 8.5f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.5f, 7.5f)
                    moveTo(13.5f, 14.5f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.5f, 15.5f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.5f, 16.5f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12.5f, 15.5f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.5f, 14.5f)
                    close()
                }
            }.build()
            return _peanut!!
        }

    private var _peanut: ImageVector? = null

    /**
     * MDI Cup icon for beverages
     * https://pictogrammers.com/library/mdi/icon/cup/
     */
    val Cup: ImageVector
        get() {
            if (_cup != null) {
                return _cup!!
            }
            _cup = ImageVector.Builder(
                name = "Cup",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(18f, 5f)
                    verticalLineTo(3f)
                    horizontalLineTo(6f)
                    verticalLineTo(5f)
                    lineTo(4f, 21f)
                    horizontalLineTo(20f)
                    lineTo(18f, 5f)
                    moveTo(12f, 19f)
                    curveTo(9.24f, 19f, 7f, 16.76f, 7f, 14f)
                    verticalLineTo(7f)
                    horizontalLineTo(17f)
                    verticalLineTo(14f)
                    curveTo(17f, 16.76f, 14.76f, 19f, 12f, 19f)
                    close()
                }
            }.build()
            return _cup!!
        }

    private var _cup: ImageVector? = null

    /**
     * MDI Oil icon for fats & oils
     * https://pictogrammers.com/library/mdi/icon/oil/
     */
    val Oil: ImageVector
        get() {
            if (_oil != null) {
                return _oil!!
            }
            _oil = ImageVector.Builder(
                name = "Oil",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12.19f, 2.38f)
                    curveTo(11.64f, 2.38f, 11.19f, 2.83f, 11.19f, 3.38f)
                    verticalLineTo(8.38f)
                    horizontalLineTo(10.19f)
                    curveTo(9.64f, 8.38f, 9.19f, 8.83f, 9.19f, 9.38f)
                    verticalLineTo(20.38f)
                    curveTo(9.19f, 21.49f, 10.08f, 22.38f, 11.19f, 22.38f)
                    horizontalLineTo(13.19f)
                    curveTo(14.3f, 22.38f, 15.19f, 21.49f, 15.19f, 20.38f)
                    verticalLineTo(9.38f)
                    curveTo(15.19f, 8.83f, 14.74f, 8.38f, 14.19f, 8.38f)
                    horizontalLineTo(13.19f)
                    verticalLineTo(3.38f)
                    curveTo(13.19f, 2.83f, 12.74f, 2.38f, 12.19f, 2.38f)
                    close()
                }
            }.build()
            return _oil!!
        }

    private var _oil: ImageVector? = null

    /**
     * MDI Seed icon for legumes
     * https://pictogrammers.com/library/mdi/icon/seed/
     */
    val Seed: ImageVector
        get() {
            if (_seed != null) {
                return _seed!!
            }
            _seed = ImageVector.Builder(
                name = "Seed",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 2f)
                    curveTo(7.58f, 2f, 4f, 5.58f, 4f, 10f)
                    curveTo(4f, 14.42f, 7.58f, 18f, 12f, 18f)
                    curveTo(16.42f, 18f, 20f, 14.42f, 20f, 10f)
                    curveTo(20f, 5.58f, 16.42f, 2f, 12f, 2f)
                    moveTo(12f, 4f)
                    curveTo(15.31f, 4f, 18f, 6.69f, 18f, 10f)
                    curveTo(18f, 13.31f, 15.31f, 16f, 12f, 16f)
                    curveTo(8.69f, 16f, 6f, 13.31f, 6f, 10f)
                    curveTo(6f, 6.69f, 8.69f, 4f, 12f, 4f)
                    moveTo(7f, 22f)
                    horizontalLineTo(17f)
                    verticalLineTo(20f)
                    horizontalLineTo(7f)
                    verticalLineTo(22f)
                    close()
                }
            }.build()
            return _seed!!
        }

    private var _seed: ImageVector? = null

    /**
     * MDI Sprout icon for legumes
     * https://pictogrammers.com/library/mdi/icon/sprout/
     */
    val Sprout: ImageVector
        get() {
            if (_sprout != null) {
                return _sprout!!
            }
            _sprout = ImageVector.Builder(
                name = "Sprout",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(2f, 22f)
                    verticalLineTo(20f)
                    curveTo(2f, 20f, 7f, 18f, 12f, 18f)
                    curveTo(17f, 18f, 22f, 20f, 22f, 20f)
                    verticalLineTo(22f)
                    horizontalLineTo(2f)
                    moveTo(11.3f, 9.1f)
                    curveTo(10.1f, 5.2f, 4f, 6.1f, 4f, 6.1f)
                    curveTo(4f, 6.1f, 4.2f, 13.9f, 9.9f, 12.7f)
                    curveTo(9.5f, 9.8f, 8f, 9f, 8f, 9f)
                    curveTo(10.8f, 9f, 11f, 12.4f, 11f, 12.4f)
                    verticalLineTo(17f)
                    curveTo(11.3f, 17f, 11.7f, 17f, 12f, 17f)
                    curveTo(12.3f, 17f, 12.7f, 17f, 13f, 17f)
                    verticalLineTo(12.8f)
                    curveTo(13f, 12.8f, 13f, 8.9f, 16f, 7.9f)
                    curveTo(16f, 7.9f, 14f, 10.9f, 14f, 12.9f)
                    curveTo(21f, 13.6f, 21f, 4f, 21f, 4f)
                    curveTo(21f, 4f, 12.1f, 3f, 11.3f, 9.1f)
                    close()
                }
            }.build()
            return _sprout!!
        }

    private var _sprout: ImageVector? = null

    /**
     * MDI Coffee icon for beverages
     * https://pictogrammers.com/library/mdi/icon/coffee/
     */
    val Coffee: ImageVector
        get() {
            if (_coffee != null) {
                return _coffee!!
            }
            _coffee = ImageVector.Builder(
                name = "Coffee",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(2f, 21f)
                    horizontalLineTo(20f)
                    verticalLineTo(19f)
                    horizontalLineTo(2f)
                    moveTo(20f, 8f)
                    horizontalLineTo(18f)
                    verticalLineTo(5f)
                    horizontalLineTo(20f)
                    moveTo(20f, 3f)
                    horizontalLineTo(4f)
                    verticalLineTo(13f)
                    arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 17f)
                    horizontalLineTo(14f)
                    arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18f, 13f)
                    verticalLineTo(10f)
                    horizontalLineTo(20f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22f, 8f)
                    verticalLineTo(5f)
                    curveTo(22f, 3.89f, 21.1f, 3f, 20f, 3f)
                    close()
                }
            }.build()
            return _coffee!!
        }

    private var _coffee: ImageVector? = null

    /**
     * MDI Water icon for fats & oils
     * https://pictogrammers.com/library/mdi/icon/water/
     */
    val Water: ImageVector
        get() {
            if (_water != null) {
                return _water!!
            }
            _water = ImageVector.Builder(
                name = "Water",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 20f)
                    arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 14f)
                    curveTo(6f, 10f, 12f, 3.25f, 12f, 3.25f)
                    curveTo(12f, 3.25f, 18f, 10f, 18f, 14f)
                    arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 20f)
                    close()
                }
            }.build()
            return _water!!
        }

    private var _water: ImageVector? = null

    /**
     * MDI Ice-Pop icon for sweets
     * https://pictogrammers.com/library/mdi/icon/ice-pop/
     */
    val IcePop: ImageVector
        get() {
            if (_icePop != null) {
                return _icePop!!
            }
            _icePop = ImageVector.Builder(
                name = "IcePop",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(18.15f, 14.96f)
                    lineTo(9.95f, 18.65f)
                    lineTo(5.85f, 9.65f)
                    curveTo(7.21f, 9.13f, 8.12f, 7.82f, 8.15f, 6.36f)
                    curveTo(8.14f, 5f, 7.36f, 3.76f, 6.15f, 3.15f)
                    curveTo(6.54f, 2.8f, 7f, 2.5f, 7.45f, 2.25f)
                    curveTo(9.71f, 1.25f, 12.37f, 2.23f, 13.45f, 4.46f)
                    moveTo(13.15f, 18.36f)
                    lineTo(14.75f, 21.86f)
                    lineTo(17.45f, 20.65f)
                    lineTo(15.85f, 17.15f)
                    close()
                }
            }.build()
            return _icePop!!
        }

    private var _icePop: ImageVector? = null

    /**
     * MDI Hamburger icon for processed foods
     * https://pictogrammers.com/library/mdi/icon/hamburger/
     */
    val Hamburger: ImageVector
        get() {
            if (_hamburger != null) {
                return _hamburger!!
            }
            _hamburger = ImageVector.Builder(
                name = "Hamburger",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(22f, 13f)
                    curveTo(22f, 14.11f, 21.11f, 15f, 20f, 15f)
                    horizontalLineTo(4f)
                    curveTo(2.9f, 15f, 2f, 14.11f, 2f, 13f)
                    reflectiveCurveTo(2.9f, 11f, 4f, 11f)
                    horizontalLineTo(13f)
                    lineTo(15.5f, 13f)
                    lineTo(18f, 11f)
                    horizontalLineTo(20f)
                    curveTo(21.11f, 11f, 22f, 11.9f, 22f, 13f)
                    moveTo(12f, 3f)
                    curveTo(3f, 3f, 3f, 9f, 3f, 9f)
                    horizontalLineTo(21f)
                    curveTo(21f, 9f, 21f, 3f, 12f, 3f)
                    moveTo(3f, 18f)
                    curveTo(3f, 19.66f, 4.34f, 21f, 6f, 21f)
                    horizontalLineTo(18f)
                    curveTo(19.66f, 21f, 21f, 19.66f, 21f, 18f)
                    verticalLineTo(17f)
                    horizontalLineTo(3f)
                    verticalLineTo(18f)
                    close()
                }
            }.build()
            return _hamburger!!
        }

    private var _hamburger: ImageVector? = null
}
