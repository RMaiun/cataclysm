package com.mairo.cataclysm.core.validation;

import java.util.List;

public interface ValidationFunction<T> {

  List<String> validate(ValueField<T> data);
}
