package com.mairo.cataclysm.validation;

import java.util.List;

public interface ValidationFunction<T> {

  List<String> validate(ValueField<T> data);
}
