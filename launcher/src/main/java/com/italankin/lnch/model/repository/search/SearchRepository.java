package com.italankin.lnch.model.repository.search;

import com.italankin.lnch.model.repository.search.match.Match;

import java.util.List;

public interface SearchRepository {

    List<? extends Match> search(CharSequence constraint);
}
