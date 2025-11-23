package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JavaScriptObject;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public final class MoveResponseDTO extends JavaScriptObject {

  // Required protected constructor for all overlay types
  protected MoveResponseDTO() {}

  // Factory method
  public static native MoveResponseDTO create() /*-{
    return {};
  }-*/;

  // ----- Pawn fields -----
  public native PawnDTO getPawn1() /*-{
    return this.pawn1;
  }-*/;

  public native void setPawn1(PawnDTO pawn1) /*-{
    this.pawn1 = pawn1;
  }-*/;

  public native PawnDTO getPawn2() /*-{
    return this.pawn2;
  }-*/;

  public native void setPawn2(PawnDTO pawn2) /*-{
    this.pawn2 = pawn2;
  }-*/;

  public native PawnDTO getPawnKilledByPawn1() /*-{
    return this.pawnKilledByPawn1;
  }-*/;

  public native void setPawnKilledByPawn1(PawnDTO pawn) /*-{
    this.pawnKilledByPawn1 = pawn;
  }-*/;

  public native PawnDTO getPawnKilledByPawn2() /*-{
    return this.pawnKilledByPawn2;
  }-*/;

  public native void setPawnKilledByPawn2(PawnDTO pawn) /*-{
    this.pawnKilledByPawn2 = pawn;
  }-*/;

  // ----- MoveSet fields -----
  public native JsArray<PositionKeyDTO> getMovePawn1() /*-{
    return this.movePawn1;
  }-*/;

  public native void setMovePawn1(JsArray<PositionKeyDTO> moveSet) /*-{
    this.movePawn1 = moveSet;
  }-*/;

  public native JsArray<PositionKeyDTO> getMovePawn2() /*-{
    return this.movePawn2;
  }-*/;

  public native void setMovePawn2(JsArray<PositionKeyDTO> moveSet) /*-{
    this.movePawn2 = moveSet;
  }-*/;

  public native JsArray<PositionKeyDTO> getMovePawnKilledByPawn1() /*-{
    return this.movePawnKilledByPawn1;
  }-*/;

  public native void setMovePawnKilledByPawn1(JsArray<PositionKeyDTO> moveSet) /*-{
    this.movePawnKilledByPawn1 = moveSet;
  }-*/;

  public native JsArray<PositionKeyDTO> getMovePawnKilledByPawn2() /*-{
    return this.movePawnKilledByPawn2;
  }-*/;

  public native void setMovePawnKilledByPawn2(JsArray<PositionKeyDTO> moveSet) /*-{
    this.movePawnKilledByPawn2 = moveSet;
  }-*/;

  // ----- MoveType -----
  public native String getMoveType() /*-{
    return this.moveType;
  }-*/;

  public native void setMoveType(String moveType) /*-{
    this.moveType = moveType;
  }-*/;

  // ----- Result -----
  public native String getResult() /*-{
    return this.result;
  }-*/;

  public native void setResult(String result) /*-{
    this.result = result;
  }-*/;
}

