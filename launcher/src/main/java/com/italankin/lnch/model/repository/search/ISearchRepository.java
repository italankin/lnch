package com.italankin.lnch.model.repository.search;

import com.italankin.lnch.model.repository.search.match.IMatch;

import java.util.List;

public interface ISearchRepository {

    List<? extends IMatch> search(CharSequence constraint);

}
