/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009 C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************************************************/
package org.mt4j.input.inputProcessors.globalProcessors;

import java.util.HashMap;
import java.util.Map;

import org.mt4j.components.IMTTargetable;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.input.IHitTestInfoProvider;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTComponent3DInputEvent;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt;
import org.mt4j.input.inputProcessors.globalProcessors.osc.GlobalOSCInputRedirectSingleton;

import de.sciss.net.OSCMessage;

/**
 * The Class InputRetargeter. This global input analyzer is automatically created with each new scene and listens
 * to all InputSources for AbstractCursorInputEvt events (Input events with a discrete position attached).
 * <br>This global input analyzer uses MTPositionEvents to check which object in the current scene was hit.
 * Then the targetComponent is added to the event and the event is delivered to the scenes canvas where the
 * targeted event is delivered to the targetComponent. No new event is created for performance reasons.
 * So the event is merely retargeted and redirected.
 *
 * @author Christopher Ruff
 */
public class InputRetargeter extends AbstractGlobalInputProcessor<MTInputEvent> {
    private Map<InputCursor, IMTComponent3D> cursorToObjectMap;

    /**
     * The app info provider.
     */
    private IHitTestInfoProvider appInfoProvider;

    public InputRetargeter(IHitTestInfoProvider appInfoProvider) {
        super();
        this.appInfoProvider = appInfoProvider;
        this.cursorToObjectMap = new HashMap<InputCursor, IMTComponent3D>();
    }


    @Override
    protected boolean canHandleEvent(MTInputEvent e) {
        return true;
    }

    @Override
    public void processInputEvtImpl(MTInputEvent inputEvent) {
        if (inputEvent instanceof MTOSCControllerInputEvt) {
            MTOSCControllerInputEvt oscEvent = (MTOSCControllerInputEvt) inputEvent;
            GlobalOSCInputProcessor target;

            OSCMessage msg = oscEvent.getControllerMessage();

            target = GlobalOSCInputRedirectSingleton.getInstance()
                    .getTargetFromUrl(msg.getName());

            if (target != null) {
                //oscEvent.setTarget(target)
                //target.processInputEvent(inputEvent);
            }
        }

        if (inputEvent instanceof MTComponent3DInputEvent) {
            AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt) inputEvent;
            InputCursor m = posEvt.getCursor();

            switch (posEvt.getId()) {
                case AbstractCursorInputEvt.INPUT_DETECTED: {
//				logger.debug("Finger DOWN-> " + " ID:" + posEvt.getId() + "; X:" + posEvt.getPosX() + " Y:" + posEvt.getPosY() + "; Source: " + posEvt.getSource());
//				System.out.println("Finger DOWN-> " + " ID:" + posEvt.getId() + "; X:" + posEvt.getPosX() + " Y:" + posEvt.getPosY() + "; Source: " + posEvt.getSource()+  " CursorID: " + m.getId() + " appInfoProv: " + appInfoProvider);
                    //Check if there is an object under the cursor and save it to a hashtable with the event if so
                    IMTComponent3D obj = appInfoProvider.getComponentAt(posEvt.getScreenX(), posEvt.getScreenY());
                    if (obj != null) {
                        cursorToObjectMap.put(m, obj);
                        posEvt.setTarget(obj);
//					posEvt.setCurrentTarget(obj.getRoot()); //Enable this if using event CAPTURING PHASE
                        posEvt.setCurrentTarget(obj);
                        posEvt.setEventPhase(MTComponent3DInputEvent.CAPTURING_PHASE);
                        this.fireInputEvent(posEvt);
                    }
                }
                break;
                case AbstractCursorInputEvt.INPUT_UPDATED: {

//				logger.debug("Finger UPDATE-> " + " ID:" + posEvt.getId() + "; X:" + posEvt.getPositionX() + " Y:" + posEvt.getPositionY() + "; Source: " + posEvt.getSource());
                    IMTComponent3D associatedObj = cursorToObjectMap.get(m);
                    if (associatedObj != null) {
                        posEvt.setTarget(associatedObj);
//					posEvt.setCurrentTarget(associatedObj.getRoot());//Enable this if using event CAPTURING PHASE
                        posEvt.setCurrentTarget(associatedObj);
                        posEvt.setEventPhase(MTComponent3DInputEvent.CAPTURING_PHASE);
                        this.fireInputEvent(posEvt);
                    }
                }
                break;
                case AbstractCursorInputEvt.INPUT_ENDED: {
//				logger.debug("Finger UP-> " + " ID:" + posEvt.getId() + "; X:" + posEvt.getPositionX() + " Y:" + posEvt.getPositionY() + "; Source: " + posEvt.getSource());
//				IMTComponent3D associatedObj = motionToObjectMap.get(m);
                    IMTComponent3D associatedObj = cursorToObjectMap.remove(m);
                    if (associatedObj != null) {
                        posEvt.setTarget(associatedObj);
//					posEvt.setCurrentTarget(associatedObj.getRoot());//Enable this if using event CAPTURING PHASE
                        posEvt.setCurrentTarget(associatedObj);
                        posEvt.setEventPhase(MTComponent3DInputEvent.CAPTURING_PHASE);
                        this.fireInputEvent(posEvt);
//					motionToObjectMap.remove(m);
                    }
                }
                break;
                default:
                    break;
            }
        } else {
            //Other event type, evtl ohne absolute x,y coordianten (z.b. joystick)
            //einfach an mtcanvas weiterleiten?
//			logger.error("Warning in " + this  + " Dont know how to handle evt: " + inputEvent );

            //Just fire other input events to the current canvas by default
            //this.fireInputEvent(inputEvent);
        }
    }


}
