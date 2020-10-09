package coverage.tools

/**
  * void VlcTop::writeInfo(const string& filename) {
  * UINFO(2, "writeInfo " << filename << endl);
  *
  * std::ofstream os(filename.c_str());
  * if (!os) {
  * v3fatal("Can't write " << filename);
  * return;
  * }
  *
  * annotateCalc();
  *
  * // See 'man lcov' for format details
  * // TN:<trace_file_name>
  * // Source file:
  * //   SF:<absolute_path_to_source_file>
  * //   FN:<line_number_of_function_start>,<function_name>
  * //   FNDA:<execution_count>,<function_name>
  * //   FNF:<number_functions_found>
  * //   FNH:<number_functions_hit>
  * // Branches:
  * //   BRDA:<line_number>,<block_number>,<branch_number>,<taken_count_or_-_for_zero>
  * //   BRF:<number_of_branches_found>
  * //   BRH:<number_of_branches_hit>
  * // Line counts:
  * //   DA:<line_number>,<execution_count>
  * //   LF:<number_of_lines_found>
  * //   LH:<number_of_lines_hit>
  * // Section ending:
  * //   end_of_record
  *
  * os << "TN:verilator_coverage\n";
  * for (auto& si : m_sources) {
  * VlcSource& source = si.second;
  * os << "SF:" << source.name() << endl;
  * VlcSource::LinenoMap& lines = source.lines();
  * for (auto& li : lines) {
  * int lineno = li.first;
  * VlcSource::ColumnMap& cmap = li.second;
  * bool first = true;
  * vluint64_t min_count = 0;  // Minimum across all columns on line
  * for (auto& ci : cmap) {
  * VlcSourceCount& col = ci.second;
  * if (first) {
  * min_count = col.count();
  * first = false;
  * } else {
  * min_count = std::min(min_count, col.count());
  * }
  * }
  * os << "DA:" << lineno << "," << min_count << "\n";
  * }
  * os << "end_of_record\n";
  * }
  * }
  */
class WriteInfoFile {

}
