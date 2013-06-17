package main.scala.TreeQuencer

import org.mt4j.{MTApplication, Scene, Application}
import org.mt4j.util.math.Vector3D
import org.mt4j.components.MTLight
import javax.media.opengl.GL
import org.mt4j.output.audio.AudioServer
import org.mt4j.util.MTColor
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._
import org.mt4j.components.visibleComponents.shapes.MTEllipse
import org.mt4j.components.visibleComponents.widgets.{MTSlider, Slider}
import org.mt4j.types.{Rotation, Vec3d}
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.input.inputProcessors.MTGestureEvent._

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 *
 * Last modified:  17.03.13 :: 17:11
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 *
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 *
 * ^     ^
 *  ^   ^
 *  (o o)
 * {  |  }                  (Wong)
 *    "
 *
 * Don't eat the pills!
 */
object app extends Application {

  // some global values
  def apply = this
  def center = new Vector3D(width/2f, height/2f)
  var scene: NodeScene = null
  var light: MTLight = null
  val globalNodeSet = new NodeSet[Node]() // all nodes are globally stored here
  val RANDOM_GAME = 0
  val SEQUENCE_GAME = 1
  val TIMESHIFT_GAME = 2
  var game = 0
  var _idCounter = 1
  def idCounter = {_idCounter+=1; _idCounter-1}
  val innerCircleRadius = 100


  def main(args: Array[String]) {
    execute(false)
  }

  override def startUp() {
    // start scene
    addScene(new NodeScene())
  }

  /**
   * Quit application, when pressing escape on keyboard
   * @param e The key event
   */
  override protected def handleKeyEvent(e: KeyEvent) {
    if (keyPressed && keyCode == VK_ESCAPE) {
      AudioServer.quit // quit supercollider server scsynth
      Runtime.getRuntime.halt(0) // quit java runtime environment
    }
    super.handleKeyEvent(e)
  }

}

class NodeScene() extends Scene(app,"TreeQuencer") {
  AudioServer.start(true)
  app.scene = this
  app.scene.setClearColor(new MTColor(255,255,255))
  app.light = new MTLight(app, GL.GL_LIGHT3, new Vector3D(0,0,200))//app.center.getAdded(app.scene.getSceneCam.getPosition))
  MTLight.enableLightningAndAmbient(app, 150, 150, 150, 255)
  showTracer(show = true)

  initializeSliders

  app.game = MTApplication.getProperties.getProperty("GameNumber", "0").toInt

  app.game match {

    case app.RANDOM_GAME =>
      println("Random game chosen")
      createInnerCircle
      SourceNode() += RandomNode()
      NodeMetronome.start()

    case app.SEQUENCE_GAME =>
      println("Sequence game chosen")
      NodeSource.buildSources
      initializeStillNodes(MTApplication.getProperties.getProperty("StillNodeCount","4").toInt)
      NodeMetronome.start()

    case app.TIMESHIFT_GAME =>
      println("Timeshift game chosen")
      createInnerCircle
      SourceNode() += RandomNode()
      Metronome().start()

    case _ =>

  }


  // method definitions

  def createInnerCircle {
    val innerCircle: MTEllipse = new MTEllipse(app, app.center, app.innerCircleRadius.toFloat, app.innerCircleRadius.toFloat)
    innerCircle.setNoFill(true)
    innerCircle.setStrokeColor(new MTColor(0,0,0))
    app.scene.canvas().addChild(innerCircle)
    innerCircle.unregisterAllInputProcessors()
    innerCircle.removeAllGestureEventListeners()
  }

  def initializeStillNodes(count: Int) {
    val y = app.height/2f
    val x = app.width - 200f
    val stillNodes = new Array[StillNode](count)
    for (i <- 0 to count-1) {
      stillNodes(i) = new StillNode(new Vector3D(i*x/(count-1)+100f,y))
      if (i>0) {
        stillNodes(i-1) += stillNodes(i)
      }
      if (i==count-1) {
        stillNodes(i) += stillNodes(0)
      }
    }
    NodeMetronome += stillNodes(0)
  }

  def initializeSliders {
    // the active slider is the one of all four, who is changed at the moment (only one at a time)
    var activeSlider = null.asInstanceOf[MTSlider]

    // create four sliders clockwise at each edge on the screen
    val sliders = new Array[MTSlider](4)

    for (i <- 0 to 3) {

      // create it
      sliders(i) = Slider(255f,1000f)
      canvas.addChild(sliders(i))

      // make it colourfull
      sliders(i).setFillColor(new MTColor(100,100,100))
      sliders(i).setStrokeColor(new MTColor(0,0,0))

      // position it
      sliders(i).globalPosition := {
        i match {
          case 0 => Vec3d(app.width/2f, 40f)
          case 1 => Vec3d(app.width-40f, app.height/2f)
          case 2 => Vec3d(app.width/2f, app.height-40f)
          case 3 => Vec3d(40f, app.height/2f)
          case _ => Vec3d()
        }
      }

      // rotate it
      sliders(i).rotateZGlobal(sliders(i).globalPosition(), i*90f)

      // add the listeners/actions for each slider
      sliders(i).addPropertyChangeListener("value", new PropertyChangeListener {
        val slider = sliders(i)
        var newValue = 0f
        def propertyChange(e: PropertyChangeEvent) {
          // only access changes from the activeSlider
          if (activeSlider != null && slider == activeSlider) {
            newValue = e.getNewValue.asInstanceOf[Float]

            // change the speed of the Metronome()
            Metronome().duration() = newValue

            // change the values of all the other sliders
            sliders.foreach( tmpSlider => {
              if (tmpSlider != slider) {
                tmpSlider.setValue(newValue)
              }
            })
          }
        }
      })

      // the knob of the slider makes its slider to activeSlider via DragEvent
      sliders(i).getKnob.addGestureListener(classOf[DragProcessor], new IGestureEventListener() {
        val slider = sliders(i)
        def processGestureEvent(ge: MTGestureEvent): Boolean = {
          if (ge.getId == GESTURE_ENDED || ge.getId == GESTURE_CANCELED) {
            activeSlider = null
          } else if (ge.getId == GESTURE_DETECTED) {
            activeSlider = slider
          }
          false
        }
      })
    }
  }

}