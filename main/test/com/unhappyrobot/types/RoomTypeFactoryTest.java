package com.unhappyrobot.types;

import com.unhappyrobot.GdxTestRunner;
import com.unhappyrobot.entities.RoomType;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.unhappyrobot.Expect.expect;

@RunWith(GdxTestRunner.class)
public class RoomTypeFactoryTest {
  @Test
  public void all_returnsKnownRoomTypes() {
    List<RoomType> types = RoomTypeFactory.all();

    expect(types.size()).toEqual(3);

    expect(types.get(0).getName()).toEqual("Generic 2x1");
    expect(types.get(1).getName()).toEqual("Generic 3x1");
    expect(types.get(2).getName()).toEqual("Generic 4x1");
  }
}
